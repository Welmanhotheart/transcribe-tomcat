package org.apache.tomcat.util.modeler;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class OperationInfo  extends FeatureInfo {

    protected final ReadWriteLock parametersLock = new ReentrantReadWriteLock();
    protected ParameterInfo parameters[] = new ParameterInfo[0];

    // ----------------------------------------------------- Instance Variables

    protected String impact = "UNKNOWN";
    protected String role = "operation";

    /**
     * @return the fully qualified Java class name of the return type for this
     * operation.
     */
    public String getReturnType() {
        if(type == null) {
            type = "void";
        }
        return type;
    }

    public void setReturnType(String returnType) {
        this.type = returnType;
    }

    /**
     * Add a new parameter to the set of arguments for this operation.
     *
     * @param parameter The new parameter descriptor
     */
    public void addParameter(ParameterInfo parameter) {

        Lock writeLock = parametersLock.writeLock();
        writeLock.lock();
        try {
            ParameterInfo results[] = new ParameterInfo[parameters.length + 1];
            System.arraycopy(parameters, 0, results, 0, parameters.length);
            results[parameters.length] = parameter;
            parameters = results;
            this.info = null;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Create and return a <code>ModelMBeanOperationInfo</code> object that
     * corresponds to the attribute described by this instance.
     * @return the operation info
     */
    MBeanOperationInfo createOperationInfo() {

        // Return our cached information (if any)
        if (info == null) {
            // Create and return a new information object
            int impact = MBeanOperationInfo.UNKNOWN;
            if ("ACTION".equals(getImpact())) {
                impact = MBeanOperationInfo.ACTION;
            } else if ("ACTION_INFO".equals(getImpact())) {
                impact = MBeanOperationInfo.ACTION_INFO;
            } else if ("INFO".equals(getImpact())) {
                impact = MBeanOperationInfo.INFO;
            }

            info = new MBeanOperationInfo(getName(), getDescription(),
                    getMBeanParameterInfo(),
                    getReturnType(), impact);
        }
        return (MBeanOperationInfo)info;
    }

    /**
     * @return the "impact" of this operation, which should be
     *  a (case-insensitive) string value "ACTION", "ACTION_INFO",
     *  "INFO", or "UNKNOWN".
     */
    public String getImpact() {
        return this.impact;
    }

    protected MBeanParameterInfo[] getMBeanParameterInfo() {
        ParameterInfo params[] = getSignature();
        MBeanParameterInfo parameters[] =
                new MBeanParameterInfo[params.length];
        for (int i = 0; i < params.length; i++) {
            parameters[i] = params[i].createParameterInfo();
        }
        return parameters;
    }

    /**
     * @return the set of parameters for this operation.
     */
    public ParameterInfo[] getSignature() {
        Lock readLock = parametersLock.readLock();
        readLock.lock();
        try {
            return this.parameters;
        } finally {
            readLock.unlock();
        }
    }

}
