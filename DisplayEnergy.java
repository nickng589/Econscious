package com.sdl.hellosdlandroid;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.smartdevicelink.managers.CompletionListener;
import com.smartdevicelink.managers.SdlManager;
import com.smartdevicelink.managers.SdlManagerListener;
import com.smartdevicelink.managers.file.filetypes.SdlArtwork;
import com.smartdevicelink.managers.screen.choiceset.ChoiceCell;
import com.smartdevicelink.managers.screen.choiceset.ChoiceSet;
import com.smartdevicelink.managers.screen.choiceset.ChoiceSetSelectionListener;
import com.smartdevicelink.managers.screen.menu.MenuCell;
import com.smartdevicelink.managers.screen.menu.MenuSelectionListener;
import com.smartdevicelink.managers.screen.menu.VoiceCommand;
import com.smartdevicelink.managers.screen.menu.VoiceCommandSelectionListener;
import com.smartdevicelink.protocol.enums.FunctionID;
import com.smartdevicelink.proxy.RPCNotification;
import com.smartdevicelink.proxy.RPCResponse;
import com.smartdevicelink.proxy.TTSChunkFactory;
import com.smartdevicelink.proxy.rpc.Alert;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.OnVehicleData;
import com.smartdevicelink.proxy.rpc.Speak;
import com.smartdevicelink.proxy.rpc.SubscribeVehicleData;
import com.smartdevicelink.proxy.rpc.enums.AppHMIType;
import com.smartdevicelink.proxy.rpc.enums.FileType;
import com.smartdevicelink.proxy.rpc.enums.HMILevel;
import com.smartdevicelink.proxy.rpc.enums.InteractionMode;
import com.smartdevicelink.proxy.rpc.enums.Result;
import com.smartdevicelink.proxy.rpc.enums.TriggerSource;
import com.smartdevicelink.proxy.rpc.listeners.OnRPCNotificationListener;
import com.smartdevicelink.proxy.rpc.listeners.OnRPCResponseListener;
import com.smartdevicelink.transport.BaseTransportConfig;
import com.smartdevicelink.transport.MultiplexTransportConfig;
import com.smartdevicelink.transport.TCPTransportConfig;
import com.smartdevicelink.util.DebugTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class DisplayEnergy {
    public DisplayEnergy(SdlManager sdl){
        _sdl = sdl;
        _sdl.getScreenManager().setTextField4("FKLSDFLKSDF");
    }

    public void display(){
        _sdl.getScreenManager().setTextField2("asdfasdf");
        _sdl.addOnRPCNotificationListener(FunctionID.ON_VEHICLE_DATA, new OnRPCNotificationListener() {
            @Override
            public void onNotified(RPCNotification notification) {
                OnVehicleData onVehicleDataNotification = (OnVehicleData) notification;
                if (onVehicleDataNotification.getDriverBraking() != null) {
                    _sdl.getScreenManager().setTextField2(onVehicleDataNotification.getDriverBraking().toString()+ "*");
                }
            }
        });
        SubscribeVehicleData subscribeRequest = new SubscribeVehicleData();
        subscribeRequest.setDriverBraking(true);
        subscribeRequest.setOnRPCResponseListener(new OnRPCResponseListener() {
            @Override
            public void onResponse(int correlationId, RPCResponse response) {
                if(response.getSuccess()){
                    Log.i("SdlService", "Successfully subscribed to vehicle data.");
                }else{
                    Log.i("SdlService", "Request to subscribe to vehicle data was rejected.");
                }
            }

            @Override
            public void onError(int correlationId, Result resultCode, String info){
                Log.e("display", "onError: "+ resultCode+ " | Info: "+ info );
            }
        });
        _sdl.sendRPC(subscribeRequest);
        _sdl.getScreenManager().commit(null);
    }
    private SdlManager _sdl;
}
