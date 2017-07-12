package com.hxh.aidlclient;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hxh.aidllib.aidl.IRemoteService;

import java.util.List;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {


    private IRemoteService mRemoteService;

    private TextView mPidText;

    private boolean mBind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPidText = (TextView) findViewById(R.id.tv_pid);
        mPidText.setText("the client pid is " + android.os.Process.myPid());
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Intent intent = new Intent();
        //intent.setClassName("com.hxh.aidl", "com.hxh.aidl.service.RemoteService");

        Intent intent = new Intent();
        intent.setAction("com.hxh.aidl.remote_service");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            intent = new Intent(createExplicitFromImplicitIntent(this,intent));
        }
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
        mBind = false;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRemoteService = IRemoteService.Stub.asInterface(service);
            mBind = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteService = null;
            mBind = false;
        }
    };

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_show_pid:
                if (mBind) {
                    try {
                        int pid = mRemoteService.sayHello().getPid();
                        Log.i("HELLO_MSG", "the service pid is " + pid);
                        mPidText.setText("the service pid is " + pid);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_say_hello:
                if (mBind) {
                    try {
                        String msg = mRemoteService.sayHello().getMsg();
                        Log.i("HELLO_MSG", msg);
                        mPidText.setText(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
                break;
        }
    }

    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }
}
