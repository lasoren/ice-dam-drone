package com.example.tberroa.girodicerapp.bluetooth;

import android.util.Log;

import com.example.tberroa.girodicerapp.data.Params;
import com.example.tberroa.girodicerapp.services.BluetoothService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused", "MismatchedQueryAndUpdateOfCollection"})
public class GProtocol {

    public static final byte COMMAND_ARM = 0x1;
    public static final byte COMMAND_UNARM = 0x2;
    public static final byte COMMAND_START_INSPECTION = 0x3;
    public static final byte COMMAND_END_INSPECTION = 0x4;
    public static final byte COMMAND_STATUS = 0x5;
    public static final byte COMMAND_SEND_ICEDAM_POINTS = 0x6;
    public static final byte COMMAND_READY_TO_TRANSFER = 0x7;
    public static final byte COMMAND_NEW_HOUSE = 0x8;
    public static final byte COMMAND_SEND_PATH = 0x9;
    public static final byte COMMAND_SEND_IMAGES_RGB = 0xA;
    public static final byte COMMAND_SEND_IMAGES_THERM = 0xB;
    public static final byte COMMAND_RETURN_HOME = 0xC;
    public static final byte COMMAND_SEND_JSON_RGB = 0xD;
    public static final byte COMMAND_SEND_JSON_THERM = 0xE;
    public static final byte COMMAND_SEND_DRONE_LANDED = 0xF;
    public static final byte COMMAND_SEND_FINISHED_DAM = 0x10;
    public static final byte COMMAND_SEND_FINISHED_ALL_DAMS = 0x11;
    public static final byte COMMAND_SEND_LOW_BATTERY = 0x12;
    public static final byte COMMAND_SEND_ROOF_SCAN_INTERRUPTED = 0x13;
    public static final byte COMMAND_SEND_BORDER_SCAN_INTERRUPTED = 0x14;
    public static final byte COMMAND_SEND_FINISHED_SCAN = 0x15;
    public static final byte COMMAND_SEND_FINISHED_BORDER = 0x16;
    public static final byte COMMAND_SEND_FINISHED_ANALYSIS = 0x17;
    public static final byte COMMAND_FINISHED_RGB = 0x18;
    public static final byte COMMAND_FINISHED_THERM = 0x19;
    public static final byte COMMAND_SERVICE_ICEDAM = 0x20;
    public static final byte COMMAND_DRONE_ALREADY_FLYING = 0x21;

    public static final byte COMMAND_BLUETOOTH_SEND_CORRUPT = 0x30;
    public static final byte COMMAND_BLUETOOTH_OK_TO_SEND = 0x31;

    public static final byte PARTIAL_CLEAR_MASK = (byte) 0x3F;
    public static final byte PARTIAL_MESSAGE = (byte) 0x80;
    public static final byte PARTIAL_LAST_MESSAGE = (byte) 0xC0;

    private final byte command;
    private final byte[] data;
    private final boolean partial;
    private final boolean partial_end;

    public static byte[] Pack(byte command, int payloadSize, byte[] array, boolean partial) {
        ByteBuffer builder = ByteBuffer.allocate(payloadSize + 1 + 4).order(ByteOrder.LITTLE_ENDIAN);

        if (partial) {
            command |= PARTIAL_MESSAGE;
        }
        builder.put(command);
        builder.putInt(payloadSize);
        builder.put(array);

        return builder.array();
    }

    public static GProtocol Unpack(byte[] array) throws BluetoothException {
        ByteBuffer builder = ByteBuffer.wrap(array);

        if (!builder.hasRemaining()) { // array passed in has nothing
            throw new BluetoothException("Empty Array", BluetoothException.ERRORS.ARRAY_EMPTY);
        }

        byte receivedCommand = builder.get();
        boolean partial = ((receivedCommand & PARTIAL_MESSAGE) == PARTIAL_MESSAGE);
        boolean partial_end = ((receivedCommand & PARTIAL_LAST_MESSAGE) == PARTIAL_LAST_MESSAGE);
        receivedCommand = (byte) (receivedCommand & PARTIAL_CLEAR_MASK);

        switch (receivedCommand) {
            case COMMAND_ARM:
            case COMMAND_UNARM:
            case COMMAND_START_INSPECTION:
            case COMMAND_END_INSPECTION:
            case COMMAND_READY_TO_TRANSFER:
            case COMMAND_SEND_FINISHED_BORDER:
            case COMMAND_SEND_FINISHED_SCAN:
            case COMMAND_FINISHED_RGB:
            case COMMAND_FINISHED_THERM:
            case COMMAND_SEND_DRONE_LANDED:
            case COMMAND_SEND_FINISHED_ANALYSIS:
            case COMMAND_SERVICE_ICEDAM:
            case COMMAND_SEND_FINISHED_DAM:
            case COMMAND_SEND_FINISHED_ALL_DAMS:
            case COMMAND_SEND_LOW_BATTERY:
            case COMMAND_SEND_ROOF_SCAN_INTERRUPTED:
            case COMMAND_SEND_BORDER_SCAN_INTERRUPTED:
            case COMMAND_RETURN_HOME:
            case COMMAND_DRONE_ALREADY_FLYING:
                BluetoothService.btConnectionThread.write(GProtocol.Pack(GProtocol.COMMAND_BLUETOOTH_OK_TO_SEND, 1, new byte[1], false));
                return new GProtocol(receivedCommand, null, false, false);
            case COMMAND_STATUS:
            case COMMAND_SEND_ICEDAM_POINTS:
            case COMMAND_SEND_PATH:
            case COMMAND_SEND_JSON_RGB:
            case COMMAND_SEND_JSON_THERM:
            case COMMAND_SEND_IMAGES_RGB:
            case COMMAND_SEND_IMAGES_THERM:
                BluetoothService.btConnectionThread.write(GProtocol.Pack(GProtocol.COMMAND_BLUETOOTH_OK_TO_SEND, 1, new byte[1], false));
                if (builder.hasRemaining()) {
                    int payloadSize = builder.getInt();
                    if (receivedCommand == COMMAND_SEND_IMAGES_RGB) {
                        Log.d(Params.TAG_DBG + Params.TAG_GP, "RGB Image payload size: " + payloadSize);
                    }
                    byte[] data = new byte[payloadSize];
                    if (builder.hasRemaining()) {
                        builder.get(data);
                        return new GProtocol(receivedCommand, data, partial, partial_end);
                    } else {
                        throw new BluetoothException("No Data", BluetoothException.ERRORS.NO_DATA);
                    }

                } else {
                    throw new BluetoothException("No payload size", BluetoothException.ERRORS.NO_SIZE);
                }
            default:
                Log.d(Params.TAG_DBG + Params.TAG_GP, "Bad Command: " + Integer.toString(receivedCommand));
                BluetoothService.btConnectionThread.write(GProtocol.Pack(GProtocol.COMMAND_BLUETOOTH_SEND_CORRUPT, 1, new byte[1], false));
                throw new BluetoothException("Bad Command", BluetoothException.ERRORS.BAD_COMMAND);
        }
    }

    public GProtocol(byte command, byte[] data, boolean partial, boolean partial_end) {
        this.command = command;
        if (data == null) {
            this.data = null;
        } else {
            this.data = data.clone();
        }
        this.partial = partial;
        this.partial_end = partial_end;
    }

    public byte getCommand() {
        return this.command;
    }

    public boolean isPartial() {
        return this.partial;
    }

    public boolean isPartialEnd() {
        return this.partial_end;
    }

    public byte[] getData() {
        return this.data;
    }

    public Object read() {
        switch (this.command) {
            case COMMAND_STATUS:
                return Status.Unpack(this.data);
            case COMMAND_SEND_ICEDAM_POINTS:
            case COMMAND_SEND_PATH:
                return Points.Unpack(this.data);
            case COMMAND_SEND_JSON_RGB:
            case COMMAND_SEND_JSON_THERM:
                return JSON.Unpack(this.data);
            case COMMAND_SEND_IMAGES_RGB:
            case COMMAND_SEND_IMAGES_THERM:
                return Images.Unpack(this.data);
        }
        return null;
    }

    public static GProtocol glueGProtocols(List<GProtocol> listGProtocols) {
        byte receivedCommand = listGProtocols.get(0).getCommand();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (GProtocol temp : listGProtocols) {
            try {
                outputStream.write(temp.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        byte[] data = outputStream.toByteArray();
        return new GProtocol(receivedCommand, data, false, true);
    }
}
