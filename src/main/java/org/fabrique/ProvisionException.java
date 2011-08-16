package org.fabrique;

/**
 * Represents an error having occured when provisioning an object instance via a provider.
 */
public class ProvisionException extends RuntimeException {
    private static final long serialVersionUID = 0;

    /**
     * Creates a new ConfigurationException object.
     * 
     * @param throwable Error message
     */
    public ProvisionException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Creates a new ConfigurationException object.
     * 
     * @param pMsg Error message
     */
    public ProvisionException(String pMsg) {
        super(pMsg);
    }

    /**
     * Creates a new ProvisionException object.
     * 
     * @param key Key that provisioning failed for
     * @param parent Parent exception
     */
    public ProvisionException(Key<?> key, Throwable parent) {
        super("Error provisioning a binding for " + key.toString(), parent);
    }

    /**
     * Creates a new ProvisionException object.
     * 
     * @param msg Error message
     * @param parent Parent exception
     */
    public ProvisionException(String msg, Throwable parent) {
        super(msg, parent);
    }
}
