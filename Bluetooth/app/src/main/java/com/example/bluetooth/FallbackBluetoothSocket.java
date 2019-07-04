package com.example.bluetooth;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class FallbackBluetoothSocket{

    private BluetoothSocket fallbackSocket;

    public FallbackBluetoothSocket(BluetoothSocket tmp){

        try
        {
            Class<?> clazz = tmp.getRemoteDevice().getClass();
            Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
            Method m = clazz.getMethod("createRfcommSocket", paramTypes);
            Object[] params = new Object[] {Integer.valueOf(1)};
            fallbackSocket = (BluetoothSocket) m.invoke(tmp.getRemoteDevice(), params);
        }
        catch (Exception e)
        {
        }
    }

    public InputStream getInputStream() throws IOException {
        return fallbackSocket.getInputStream();
    }

    public OutputStream getOutputStream() throws IOException {
        return fallbackSocket.getOutputStream();
    }


    public void connect() throws IOException {
        fallbackSocket.connect();
    }

    public void close() throws IOException {
        fallbackSocket.close();
    }

}

