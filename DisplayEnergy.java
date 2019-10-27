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
import java.lang.Math;

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
import com.smartdevicelink.proxy.rpc.UnsubscribeButton;
import com.smartdevicelink.proxy.rpc.UnsubscribeVehicleData;
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
        first = true;
        _sdl.addOnRPCNotificationListener(FunctionID.ON_VEHICLE_DATA, new OnRPCNotificationListener() {
            @Override
            public void onNotified(RPCNotification notification) {
                OnVehicleData onVehicleDataNotification = (OnVehicleData) notification;
                if (onVehicleDataNotification.getExternalTemperature() != null ||onVehicleDataNotification.getExternalTemperature() != null) {
                    if (displayTemp == true) {
                        double celcius = onVehicleDataNotification.getExternalTemperature();
                        celcius = 13.12 + celcius*0.6215-Math.pow((speed/3.6),0.16)*11.37 +0.3965*celcius*Math.pow(speed/3.6,0.16);
                        if (celcius > targetTemp || speed > 88.5) {
                            _sdl.getScreenManager().setTextField2("Use Air Conditioner");
                        } else {
                            _sdl.getScreenManager().setTextField2("Roll windows down");
                        }
                        //_sdl.getScreenManager().setTextField3(speed + "");
                    }
                }
            }
        });
        SubscribeVehicleData subscribeRequest = new SubscribeVehicleData();
        subscribeRequest.setExternalTemperature(true);
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

        _sdl.addOnRPCNotificationListener(FunctionID.ON_VEHICLE_DATA, new OnRPCNotificationListener() {
            @Override
            public void onNotified(RPCNotification notification) {
                OnVehicleData onVehicleDataNotification = (OnVehicleData) notification;
                if (onVehicleDataNotification.getSpeed() != null) {
                    speed = onVehicleDataNotification.getSpeed();
                    if (displaySpeed == true) {
                        _sdl.getScreenManager().setTextField2(speed + " KM/hr");
                    }
                }
            }
        });
        subscribeRequest = new SubscribeVehicleData();
        subscribeRequest.setSpeed(true);
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

        _sdl.addOnRPCNotificationListener(FunctionID.ON_VEHICLE_DATA, new OnRPCNotificationListener() {
            @Override
            public void onNotified(RPCNotification notification) {
                OnVehicleData onVehicleDataNotification = (OnVehicleData) notification;
                if (onVehicleDataNotification.getFuelLevel() != null) {
                    fuel = onVehicleDataNotification.getFuelLevel();
                    if(first){
                        startingFuel = fuel;
                    }
                    if (displayFuel == true) {
                        double fuelUsed = (startingFuel - fuel)/100*15;
                        _sdl.getScreenManager().setTextField2(Double.toString(fuelUsed) + " gallons used");
                        _sdl.getScreenManager().setTextField3(fuelUsed*8.887 + " kilograms of CO2 emitted");
                        first = false;
                    }
                }
            }
        });
        subscribeRequest = new SubscribeVehicleData();
        subscribeRequest.setFuelLevel(true);
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
    }

    public void display(){
        _sdl.getScreenManager().setTextField3("");
        _sdl.getScreenManager().setTextField2("");
        _sdl.getScreenManager().setTextField1("Temp");
        displayTemp = true;
    }

    public void display1(){
        _sdl.getScreenManager().setTextField3("");
        _sdl.getScreenManager().setTextField2("");
        _sdl.getScreenManager().setTextField1("Fuel Consumption");
        /*
        _sdl.addOnRPCNotificationListener(FunctionID.ON_VEHICLE_DATA, new OnRPCNotificationListener() {
            @Override
            public void onNotified(RPCNotification notification) {
                OnVehicleData onVehicleDataNotification = (OnVehicleData) notification;
                if (onVehicleDataNotification.getSpeed() != null) {
                    if (displaySpeed == true) {
                        double speed = onVehicleDataNotification.getSpeed();
                        _sdl.getScreenManager().setTextField2(speed + " KM/hr");
                    }
                }
            }
        });
        SubscribeVehicleData subscribeRequest = new SubscribeVehicleData();
        subscribeRequest.setSpeed(true);
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
        */
        displayFuel = true;
    }

    public void stop(){
        displayFuel = false;
        displayTemp = false;
        displaySpeed = false;
    }

    private SdlManager _sdl;
    private boolean displaySpeed;
    private boolean displayTemp;
    private boolean displayFuel;
    private double targetTemp = 24;
    private double speed;
    private boolean first;
    private double fuel;
    private double startingFuel;
}
