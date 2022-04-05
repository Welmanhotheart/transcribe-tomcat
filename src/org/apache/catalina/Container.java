package org.apache.catalina;

import org.apache.juli.logging.Log;

import javax.management.ObjectName;
import java.io.File;

public interface Container extends Lifecycle{


    /**
     * The ContainerEvent event type sent when a child container is added
     * by <code>addChild()</code>.
     */
    public static final String ADD_CHILD_EVENT = "addChild";


    /**
     * The ContainerEvent event type sent when a valve is removed
     * by <code>removeValve()</code>, if this Container supports pipelines.
     */
    public static final String REMOVE_VALVE_EVENT = "removeValve";

    /**
     * The ContainerEvent event type sent when a valve is added
     * by <code>addValve()</code>, if this Container supports pipelines.
     */
    public static final String ADD_VALVE_EVENT = "addValve";


    /**
     * The ContainerEvent event type sent when a child container is removed
     * by <code>removeChild()</code>.
     */
    public static final String REMOVE_CHILD_EVENT = "removeChild";


    /**
     * Notify all container event listeners that a particular event has
     * occurred for this Container.  The default implementation performs
     * this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param data Event data
     */
    public void fireContainerEvent(String type, Object data);


    public void setParentClassLoader(ClassLoader parent);

    /**
     * Set a name string (suitable for use by humans) that describes this
     * Container.  Within the set of child containers belonging to a particular
     * parent, Container names must be unique.
     *
     * @param name New name of this container
     *
     * @exception IllegalStateException if this Container has already been
     *  added to the children of a parent Container (after which the name
     *  may not be changed)
     */
    public void setName(String name);


    /**
     * Return a name string (suitable for use by humans) that describes this
     * Container.  Within the set of child containers belonging to a particular
     * parent, Container names must be unique.
     *
     * @return The human readable name of this container.
     */
    public String getName();

    /**
     * Obtain the Realm with which this Container is associated.
     *
     * @return The associated Realm; if there is no associated Realm, the
     *         Realm associated with the parent Container (if any); otherwise
     *         return <code>null</code>.
     */
    public Realm getRealm();



    /**
     * Remove an existing child Container from association with this parent
     * Container.
     *
     * @param child Existing child Container to be removed
     */
    public void removeChild(Container child);


    /**
     * Add a new child Container to those associated with this Container,
     * if supported.  Prior to adding this Container to the set of children,
     * the child's <code>setParent()</code> method must be called, with this
     * Container as an argument.  This method may thrown an
     * <code>IllegalArgumentException</code> if this Container chooses not
     * to be attached to the specified Container, in which case it is not added
     *
     * @param child New child Container to be added
     *
     * @exception IllegalArgumentException if this exception is thrown by
     *  the <code>setParent()</code> method of the child Container
     * @exception IllegalArgumentException if the new child does not have
     *  a name unique from that of existing children of this Container
     * @exception IllegalStateException if this Container does not support
     *  child Containers
     */
    public void addChild(Container child);

    /**
     * Obtain the location of CATALINA_BASE.
     *
     * @return  The location of CATALINA_BASE.
     */
    public File getCatalinaBase();


    /**
     * Obtain the child Containers associated with this Container.
     *
     * @return An array containing all children of this container. If this
     *         Container has no children, a zero-length array is returned.
     */
    public Container[] findChildren();

    /**
     * Obtain a child Container by name.
     *
     * @param name Name of the child Container to be retrieved
     *
     * @return The child Container with the given name or <code>null</code> if
     *         no such child exists.
     */
    public Container findChild(String name);



    /**
     * Set the Realm with which this Container is associated.
     *
     * @param realm The newly associated Realm
     */
    public void setRealm(Realm realm);


    /**
     * Calculate the key properties string to be added to an object's
     * {@link ObjectName} to indicate that it is associated with this container.
     *
     * @return          A string suitable for appending to the ObjectName
     *
     */
    public String getMBeanKeyProperties();

    /**
     * Return the Pipeline object that manages the Valves associated with
     * this Container.
     *
     * @return The Pipeline
     */
    public Pipeline getPipeline();


    /**
     * Obtain the JMX domain under which this container will be / has been
     * registered.
     *
     * @return The JMX domain name
     */
    public String getDomain();

    /**
     * Return the logger name that the container will use.
     * @return the abbreviated name of this container for logging messages
     */
    public String getLogName();

    /**
     * Obtain the log to which events for this container should be logged.
     *
     * @return The Logger with which this Container is associated.  If there is
     *         no associated Logger, return the Logger associated with the
     *         parent Container (if any); otherwise return <code>null</code>.
     */
    public Log getLogger();


    /**
     * Get the parent container.
     *
     * @return Return the Container for which this Container is a child, if
     *         there is one. If there is no defined parent, return
     *         <code>null</code>.
     */
    public Container getParent();


    /**
     * Get the parent class loader.
     *
     * @return the parent class loader for this component. If not set, return
     *         {@link #getParent()}.{@link #getParentClassLoader()}. If no
     *         parent has been set, return the system class loader.
     */
    public ClassLoader getParentClassLoader();



    /**
     * Set the parent Container to which this Container is being added as a
     * child.  This Container may refuse to become attached to the specified
     * Container by throwing an exception.
     *
     * @param container Container to which this Container is being added
     *  as a child
     *
     * @exception IllegalArgumentException if this Container refuses to become
     *  attached to the specified Container
     */
    public void setParent(Container container);

    /**
     * Add a container event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addContainerListener(ContainerListener listener);



}
