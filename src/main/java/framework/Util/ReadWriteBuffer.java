package framework.Util;

import java.nio.ByteBuffer;

public class ReadWriteBuffer {
    public ByteBuffer readBuffer;
    public ByteBuffer writeBuffer;
    public ReadWriteBuffer(){
        readBuffer = ByteBuffer.allocate(1024);
        writeBuffer = ByteBuffer.allocate(1024);
    }
}
