/*
Example:
./ilb ip "https://api.ipify.org" 0x1
./ilb mark /tmp/go.sock
*/

package main

import (
	"encoding/binary"
	"fmt"
	"io/ioutil"
	"log"
	"net"
	"net/http"
	"os"
	"strconv"
	"syscall"
	"time"
)

func serv(via *net.UnixConn) {
	defer via.Close()

	// get the underlying socket
	viaf, err := via.File()
	if err != nil {
		log.Fatal(err)
	}
	socket := int(viaf.Fd())
	defer viaf.Close()

	syscall.SetNonblock(socket, false)

	// recvmsg
	buf := make([]byte, syscall.CmsgSpace(4))
	_, _, _, _, err = syscall.Recvmsg(socket, nil, buf, 0)
	if err != nil {
		log.Fatal(err)
	}

	// parse control msgs
	var msgs []syscall.SocketControlMessage
	msgs, err = syscall.ParseSocketControlMessage(buf)

	var mark int64
	err = binary.Read(via, binary.BigEndian, &mark)

	for i := 0; i < len(msgs) && err == nil; i++ {
		var fds []int
		fds, err = syscall.ParseUnixRights(&msgs[i])
		for _, fd := range fds {
			// fmt.Printf(" >>> mark: fd = %d, mark = %d\n", fd, mark)
			err := syscall.SetsockoptInt(int(fd), syscall.SOL_SOCKET, syscall.SO_MARK, int(mark))
			if err != nil {
				log.Printf(" >>> mark error: %s", err)
			}
			via.Write([]byte("ok"))
		}
	}
}

func main() {
	if len(os.Args) <= 2 {
		log.Fatalf("usage: %s command address mark(s)\n", os.Args[0])
		return
	}

	command := os.Args[1]
	address := os.Args[2]

	if command == "mark" {
		// address: file name

		fmt.Printf("ilb mark: address: %s\n", address)

		var l net.Listener
		l, err := net.Listen("unix", address)
		if err != nil {
			log.Fatalf("mark error: %s\n", err)
		}
		defer l.Close()

		for {
			var a net.Conn
			a, err = l.Accept()
			if err != nil {
				fmt.Printf("mark error: %s\n", err)
				return
			}
			go serv(a.(*net.UnixConn))
		}

	} else if command == "ip" {

		m, e := strconv.ParseInt(os.Args[3], 0, 64)
		if e != nil {
			log.Fatalf("bad args\n")
			return
		}
		mark := int(m)

		var DefaultDialer = &net.Dialer{
			Timeout:   2 * time.Second,
			KeepAlive: 2 * time.Second,
			Control: func(network, address string, c syscall.RawConn) error {
				return c.Control(func(fd uintptr) {
					err := syscall.SetsockoptInt(int(fd), syscall.SOL_SOCKET, syscall.SO_MARK, mark)
					if err != nil {
						log.Printf("control: %s", err)
						return
					}
				})
			},
		}

		tr := &http.Transport{

			Dial: DefaultDialer.Dial,

			TLSHandshakeTimeout: 2 * time.Second,
		}

		client := http.Client{Transport: tr}

		res, _ := client.Get(address)
		ip, _ := ioutil.ReadAll(res.Body)
		os.Stdout.Write(ip)
		fmt.Println()
	} else {
		log.Fatalln("Bad command")
	}
}
