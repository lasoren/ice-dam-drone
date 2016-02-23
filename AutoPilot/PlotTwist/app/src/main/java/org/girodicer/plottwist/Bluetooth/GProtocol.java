package org.girodicer.plottwist.Bluetooth;

import org.girodicer.plottwist.Models.Points;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Carlos on 2/21/2016.
 *
 * This class is for handling the communicaton protocol between the missionplanner and the android app
 *
 * Possible commands are defined
 *
 * Incoming messages must have the protocol message 'Unpacked' then have the message be 'read'
 *  To know what object is being return upon 'read', the command must be deciphered
 */
public class GProtocol {
    public static final byte COMMAND_ARM = 0x1;
    public static final byte COMMAND_UNARM = 0x2;
    public static final byte COMMAND_START_INSPECTION = 0x3;
    public static final byte COMMAND_END_INSPECTION = 0x4;
    public static final byte COMMAND_STATUS = 0x5;
    public static final byte COMMAND_SEND_POINTS = 0x6;
    public static final byte COMMAND_READY_TO_TRANSFER = 0x7;

    public static final byte PARTIAL_MESSAGE = (byte) 0x80;

    private byte command;
    private byte[] data;
    private boolean partial;

    public static byte[] Pack(byte command, int payloadSize, byte[] array, boolean partial){
        ByteBuffer builder = ByteBuffer.allocate(payloadSize + 1).order(ByteOrder.LITTLE_ENDIAN);

        if(partial){
            command |= PARTIAL_MESSAGE;
        }
        builder.put(command);
        builder.putInt(payloadSize);
        builder.put(array);

        return builder.array();
    }

    public static GProtocol Unpack(byte[] array) throws BluetoothException{
        ByteBuffer builder = ByteBuffer.wrap(array).order(ByteOrder.BIG_ENDIAN);

        if(!builder.hasRemaining()){ // array passed in has nothing
            throw new BluetoothException("Empty Array", BluetoothException.ERRORS.ARRAY_EMPTY);
        }

        byte receivedCommand = builder.get();

        switch(receivedCommand){
            case COMMAND_ARM:
            case COMMAND_UNARM:
            case COMMAND_START_INSPECTION:
            case COMMAND_END_INSPECTION:
            case COMMAND_READY_TO_TRANSFER:
                return new GProtocol(receivedCommand, null, false);
            case COMMAND_STATUS:
            case COMMAND_SEND_POINTS:
                boolean partial = ((receivedCommand & PARTIAL_MESSAGE) != 0x0);
                if(builder.hasRemaining()){
                    int payloadSize = builder.getInt();
                    byte[] data = new byte[payloadSize];

                    if(builder.hasRemaining()){
                        builder.get(data);
                        return new GProtocol(receivedCommand, data, partial);
                    } else {
                        throw new BluetoothException("No Data", BluetoothException.ERRORS.NO_DATA);
                    }

                } else {
                    throw new BluetoothException("No payload size", BluetoothException.ERRORS.NO_SIZE);
                }
            default:
                throw new BluetoothException("Bad Command", BluetoothException.ERRORS.BAD_COMMAND);
        }
    }

    public GProtocol(byte command, byte[] data, boolean partial){
        this.command = command;
        this.data = data;
        this.partial = partial;
    }

    public byte getCommand(){
        return this.command;
    }

    public Object read(){
        switch(this.command){
            case COMMAND_SEND_POINTS:
                return Points.Unpack(this.data);
        }
        return null;
    }
}
