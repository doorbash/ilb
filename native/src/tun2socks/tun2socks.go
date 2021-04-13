// package ilbroot
package tun2socks

import (
	"encoding/binary"
	"errors"
	"fmt"
	"log"
	"net"
	"strconv"
	"strings"
	"syscall"
	"time"

	"github.com/doorbash/go-tun2socks-mobile/proxy"

	"github.com/eycorsican/go-tun2socks/core"
)

var isStopped = false
var marks []int64
var tcpIndex int = 0
var udpIndex int = 0
var markFile string

type VpnService interface {
	Protect(fd int) bool
}

type PacketFlow interface {
	WritePacket(packet []byte)
}

var lwipStack core.LWIPStack
var vpnService VpnService

func InputPacket(data []byte) {
	lwipStack.Write(data)
}

// SetNonblock puts the fd in blocking or non-blocking mode.
func SetNonblock(fd int, nonblocking bool) bool {
	err := syscall.SetNonblock(fd, nonblocking)
	if err != nil {
		return false
	}
	return true
}

func protectFd(s VpnService, fd int) error {
	if s.Protect(fd) {
		return nil
	} else {
		return errors.New(fmt.Sprintf("failed to protect fd %v", fd))
	}
}

func markFd(fd int, mark int64) error {
	via, err := net.Dial("unix", markFile)
	if err != nil {
		return err
	}
	defer via.Close()

	viaf, err := via.(*net.UnixConn).File()
	if err != nil {
		return err
	}
	socket := int(viaf.Fd())
	defer viaf.Close()

	rights := syscall.UnixRights(fd)
	err = syscall.Sendmsg(socket, nil, rights, nil, 0)

	if err != nil {
		return err
	}

	err = binary.Write(via, binary.BigEndian, mark)
	if err != nil {
		return err
	}

	if err != nil {
		return err
	}

	buf := make([]byte, 2)
	_, err = via.Read(buf)

	if err != nil {
		return err
	}

	if string(buf) != "ok" {
		return errors.New("buf not ok")
	}

	return nil
}

func Start(serv VpnService, packetFlow PacketFlow, markF string, m string, proxyHost string, proxyPort int) {
	if packetFlow != nil {
		isStopped = false
		tcpIndex = 0
		udpIndex = 0
		vpnService = serv
		markFile = markF

		lwipStack = core.NewLWIPStack()

		m := strings.Split(m, " ")
		marks = make([]int64, 0)
		for _, v := range m {
			mark, e := strconv.ParseInt(v, 0, 64)
			if e != nil {
				log.Fatalf("bad marks")
				return
			}
			marks = append(marks, int64(mark))
		}

		core.RegisterTCPConnHandler(proxy.NewTCPHandler(func(fd uintptr) {
			if err := markFd(int(fd), marks[tcpIndex]); err != nil {
				log.Printf("markFd error: %s\n", err)
			}
			if err := protectFd(serv, int(fd)); err != nil {
				log.Printf("protectFd error: %s\n", err)
			}
			tcpIndex++
			if tcpIndex >= len(marks) {
				tcpIndex = 0
			}
		}))

		core.RegisterUDPConnHandler(proxy.NewUDPHandler(10*time.Second, func(fd int) {
			if err := markFd(fd, marks[udpIndex]); err != nil {
				log.Printf("markFd error: %s\n", err)
			}
			if err := protectFd(serv, fd); err != nil {
				log.Printf("protectFd error: %s\n", err)
			}
			udpIndex++
			if udpIndex >= len(marks) {
				udpIndex = 0
			}
		}))

		core.RegisterOutputFn(func(data []byte) (int, error) {
			if !isStopped {
				packetFlow.WritePacket(data)
			}
			return len(data), nil
		})
	}
}

func Stop() {
	isStopped = true
	if lwipStack != nil {
		lwipStack.Close()
		lwipStack = nil
	}
	vpnService = nil
}
