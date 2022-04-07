package org.apache.naming.java;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;

public class javaURLContextFactory  implements ObjectFactory, InitialContextFactory {
    @Override
    public Context getInitialContext(Hashtable<?, ?> hashtable) throws NamingException {
        return null;
    }

    @Override
    public Object getObjectInstance(Object o, Name name, Context context, Hashtable<?, ?> hashtable) throws Exception {
        return null;
    }
}
