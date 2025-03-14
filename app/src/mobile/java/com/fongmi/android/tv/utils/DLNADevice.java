package com.fongmi.android.tv.utils;

import com.android.cast.dlna.dmc.DLNACastManager;
import com.fongmi.android.tv.bean.Device;

import java.util.ArrayList;
import java.util.List;

public class DLNADevice {

    private final List<org.fourthline.cling.model.meta.Device<?, ?, ?>> devices;

    private static class Loader {
        static volatile DLNADevice INSTANCE = new DLNADevice();
    }

    public static DLNADevice get() {
        return Loader.INSTANCE;
    }

    public DLNADevice() {
        this.devices = new ArrayList<>();
    }

    public boolean isEmpty() {
        return devices.isEmpty();
    }

    private Device create(org.fourthline.cling.model.meta.Device<?, ?, ?> item) {
        Device device = new Device();
        device.setUuid(item.getIdentity().getUdn().getIdentifierString());
        device.setName(item.getDetails().getFriendlyName());
        device.setType(2);
        return device;
    }

    public List<Device> getAll() {
        List<Device> items = new ArrayList<>();
        for (org.fourthline.cling.model.meta.Device<?, ?, ?> item : devices) items.add(create(item));
        return items;
    }

    public List<Device> add(org.fourthline.cling.model.meta.Device<?, ?, ?> item) {
        devices.remove(item);
        devices.add(item);
        return getAll();
    }

    public Device remove(org.fourthline.cling.model.meta.Device<?, ?, ?> device) {
        devices.remove(device);
        return create(device);
    }

    public void disconnect() {
        for (org.fourthline.cling.model.meta.Device<?, ?, ?> device : devices) DLNACastManager.INSTANCE.disconnectDevice(device);
    }

    public org.fourthline.cling.model.meta.Device<?, ?, ?> find(Device item) {
        for (org.fourthline.cling.model.meta.Device<?, ?, ?> device : devices) if (device.getIdentity().getUdn().getIdentifierString().equals(item.getUuid())) return device;
        return null;
    }
}
