/*
 * Copyright 2021 John "topjohnwu" Wu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.doorbash.ilb.core.su;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.Shell.Result;
import com.topjohnwu.superuser.internal.MainShell;
import com.topjohnwu.superuser.ipc.RootService;

import java.io.File;

import static io.github.doorbash.ilb.App.LOCAL_SOCKS5_SERVER_PORT;

public class SuService extends RootService implements Handler.Callback {

    public static final String TAG = "SuService";

    public static final int COMMAND_INIT_BUSYBOX = 0;
    public static final int COMMAND_START_MARK_SERVER = 1;
    public static final int COMMAND_STOP_MARK_SERVER = 2;

    public static final String OUTPUT = "output";

    public String socket = "";

    @Override
    public IBinder onBind(@NonNull Intent intent) {
        Log.d(TAG, "onBind");
        Handler h = new Handler(Looper.getMainLooper(), this);
        Messenger m = new Messenger(h);
        return m.getBinder();
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        Message reply = Message.obtain();
        reply.what = msg.what;
        reply.arg1 = Process.myPid();
        reply.arg2 = Process.myUid();
        Bundle data = new Bundle();
        switch (msg.what) {
            case COMMAND_INIT_BUSYBOX: {
                SUKt.initSU();
                MainShell.mainShell = null;
                Result result = Shell.su("cat --help").exec();
                Log.d(TAG, result.getOut().get(0));
//                Result result = Shell.su("echo 1").exec();
//                Log.d(TAG, result.getOut().get(0));

//                Result result1 = Shell.su("setenforce 0").exec();

                data.putString(OUTPUT, result.isSuccess() ? "ok" : "err");
                break;
            }
            case COMMAND_START_MARK_SERVER: {
                Result res = Shell.su("pkill -9 libilb.so").exec();
                if(!res.getOut().isEmpty()) Log.d(TAG, res.getOut().get(0));
                socket = msg.getData().getString("socket");
                runIlbSo();
                data.putString(OUTPUT, "ok");
                break;
            }
            case COMMAND_STOP_MARK_SERVER: {
                Shell.su("pkill -9 libilb.so").exec();
                data.putString(OUTPUT, "ok");
                break;
            }
        }
        reply.setData(data);
        try {
            msg.replyTo.send(reply);
        } catch (RemoteException e) {
            Log.e(TAG, "Remote error", e);
        }
        return false;
    }

    Shell.ResultCallback callback = result -> {
        if(!result.isSuccess()){
            Log.d(TAG, "restarting libilb.so in 3 seconds...");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runIlbSo();
        }
    };
    public void runIlbSo() {
        File lib = new File(getApplicationInfo().nativeLibraryDir, "libilb.so");
        Log.d(TAG, lib.toString() + " mark " + socket);
        Shell.su(lib.toString() + " mark " + socket).submit(callback);
    }


    @Override
    public boolean onUnbind(@NonNull Intent intent) {
        Log.d(TAG, "onUnbind, client process unbound");
        // Default returns false, which means NOT daemon mode
        return super.onUnbind(intent);
    }
}
