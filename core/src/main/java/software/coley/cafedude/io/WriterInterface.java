// WriterInterface.java
package software.coley.cafedude.io;

import software.coley.cafedude.classfile.ClassFile;
import software.coley.cafedude.classfile.attribute.Attribute;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class WriterInterface {
    protected DataOutputStream out;


    public abstract byte[] writeAttribute(Attribute attribute)  throws IOException;


}
