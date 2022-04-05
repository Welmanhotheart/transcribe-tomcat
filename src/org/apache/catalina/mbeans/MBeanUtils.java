package org.apache.catalina.mbeans;

import org.apache.catalina.*;
import org.apache.catalina.util.ContextName;
import org.apache.tomcat.util.descriptor.web.ContextEnvironment;
import org.apache.tomcat.util.descriptor.web.ContextResource;
import org.apache.tomcat.util.descriptor.web.ContextResourceLink;
import org.apache.tomcat.util.modeler.ManagedBean;
import org.apache.tomcat.util.modeler.Registry;
import org.apache.tomcat.util.res.StringManager;

import javax.management.*;

public class MBeanUtils {

// ------------------------------------------------------- Static Variables

    protected static final StringManager sm = StringManager.getManager(MBeanUtils.class);

    /**
     * The configuration information registry for our managed beans.
     */
    private static Registry registry = createRegistry();


    /**
     * The <code>MBeanServer</code> for this application.
     */
    private static MBeanServer mserver = createServer();


    /**
     * The set of exceptions to the normal rules used by
     * <code>createManagedBean()</code>.  The first element of each pair
     * is a class name, and the second element is the managed bean name.
     */
    private static final String exceptions[][] = {
            { "org.apache.catalina.users.MemoryGroup",
                    "Group" },
            { "org.apache.catalina.users.MemoryRole",
                    "Role" },
            { "org.apache.catalina.users.MemoryUser",
                    "User" },
            { "org.apache.catalina.users.GenericGroup",
                    "Group" },
            { "org.apache.catalina.users.GenericRole",
                    "Role" },
            { "org.apache.catalina.users.GenericUser",
                    "User" }
    };

    /**
     * Create and configure (if necessary) and return the
     * <code>MBeanServer</code> with which we will be
     * registering our <code>DynamicMBean</code> implementations.
     * @return the singleton MBean server
     */
    public static synchronized MBeanServer createServer() {
        if (mserver == null) {
            mserver = Registry.getRegistry(null, null).getMBeanServer();
        }
        return mserver;
    }


    /**
     * Create and return the name of the <code>ManagedBean</code> that
     * corresponds to this Catalina component.
     *
     * @param component The component for which to create a name
     */
    static String createManagedName(Object component) {

        // Deal with exceptions to the standard rule
        String className = component.getClass().getName();
        for (String[] exception : exceptions) {
            if (className.equals(exception[0])) {
                return exception[1];
            }
        }

        // Perform the standard transformation
        int period = className.lastIndexOf('.');
        if (period >= 0) {
            className = className.substring(period + 1);
        }
        return className;

    }

    /**
     * Create and configure (if necessary) and return the registry of
     * managed object descriptions.
     * @return the singleton registry
     */
    public static synchronized Registry createRegistry() {
        if (registry == null) {
            registry = Registry.getRegistry(null, null);
            ClassLoader cl = MBeanUtils.class.getClassLoader();

            registry.loadDescriptors("org.apache.catalina.mbeans",  cl);
            registry.loadDescriptors("org.apache.catalina.authenticator", cl);
            registry.loadDescriptors("org.apache.catalina.core", cl);
            registry.loadDescriptors("org.apache.catalina", cl);
            registry.loadDescriptors("org.apache.catalina.deploy", cl);
            registry.loadDescriptors("org.apache.catalina.loader", cl);
            registry.loadDescriptors("org.apache.catalina.realm", cl);
            registry.loadDescriptors("org.apache.catalina.session", cl);
            registry.loadDescriptors("org.apache.catalina.startup", cl);
            registry.loadDescriptors("org.apache.catalina.users", cl);
            registry.loadDescriptors("org.apache.catalina.ha", cl);
            registry.loadDescriptors("org.apache.catalina.connector", cl);
            registry.loadDescriptors("org.apache.catalina.valves",  cl);
            registry.loadDescriptors("org.apache.catalina.storeconfig",  cl);
            registry.loadDescriptors("org.apache.tomcat.util.descriptor.web",  cl);
        }
        return registry;
    }

    /**
     * Create, register, and return an MBean for this
     * <code>ContextEnvironment</code> object.
     *
     * @param environment The ContextEnvironment to be managed
     * @return a new MBean
     * @exception Exception if an MBean cannot be created or registered
     */
    public static DynamicMBean createMBean(ContextEnvironment environment)
            throws Exception {

        String mname = createManagedName(environment);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception(sm.getString("mBeanUtils.noManagedBean", mname));
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null) {
            domain = mserver.getDefaultDomain();
        }
        DynamicMBean mbean = managed.createMBean(environment);
        ObjectName oname = createObjectName(domain, environment);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return mbean;

    }

    /**
     * Create, register, and return an MBean for this
     * <code>ContextResource</code> object.
     *
     * @param resource The ContextResource to be managed
     * @return a new MBean
     * @exception Exception if an MBean cannot be created or registered
     */
    public static DynamicMBean createMBean(ContextResource resource)
            throws Exception {

        String mname = createManagedName(resource);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception(sm.getString("mBeanUtils.noManagedBean", mname));
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null) {
            domain = mserver.getDefaultDomain();
        }
        DynamicMBean mbean = managed.createMBean(resource);
        ObjectName oname = createObjectName(domain, resource);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return mbean;

    }

    /**
     * Create, register, and return an MBean for this
     * <code>ContextResourceLink</code> object.
     *
     * @param resourceLink The ContextResourceLink to be managed
     * @return a new MBean
     * @exception Exception if an MBean cannot be created or registered
     */
    public static DynamicMBean createMBean(ContextResourceLink resourceLink)
            throws Exception {

        String mname = createManagedName(resourceLink);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception(sm.getString("mBeanUtils.noManagedBean", mname));
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null) {
            domain = mserver.getDefaultDomain();
        }
        DynamicMBean mbean = managed.createMBean(resourceLink);
        ObjectName oname = createObjectName(domain, resourceLink);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return mbean;

    }

    /**
     * Create an <code>ObjectName</code> for this
     * <code>ContextResourceLink</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param resourceLink The ContextResourceLink to be named
     * @return a new object name
     * @exception MalformedObjectNameException if a name cannot be created
     */
    public static ObjectName createObjectName(String domain,
                                              ContextResourceLink resourceLink)
            throws MalformedObjectNameException {

        ObjectName name = null;
        String quotedResourceLinkName
                = ObjectName.quote(resourceLink.getName());
        Object container =
                resourceLink.getNamingResources().getContainer();
        if (container instanceof Server) {
            name = new ObjectName(domain + ":type=ResourceLink" +
                    ",resourcetype=Global" +
                    ",name=" + quotedResourceLinkName);
        } else if (container instanceof Context) {
            Context context = ((Context)container);
            ContextName cn = new ContextName(context.getName(), false);
            Container host = context.getParent();
            name = new ObjectName(domain + ":type=ResourceLink" +
                    ",resourcetype=Context,host=" + host.getName() +
                    ",context=" + cn.getDisplayName() +
                    ",name=" + quotedResourceLinkName);
        }

        return name;

    }


    /**
     * Create an <code>ObjectName</code> for this
     * <code>Service</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param environment The ContextEnvironment to be named
     * @return a new object name
     * @exception MalformedObjectNameException if a name cannot be created
     */
    public static ObjectName createObjectName(String domain,
                                              ContextEnvironment environment)
            throws MalformedObjectNameException {

        ObjectName name = null;
        Object container =
                environment.getNamingResources().getContainer();
        if (container instanceof Server) {
            name = new ObjectName(domain + ":type=Environment" +
                    ",resourcetype=Global,name=" + environment.getName());
        } else if (container instanceof Context) {
            Context context = ((Context)container);
            ContextName cn = new ContextName(context.getName(), false);
            Container host = context.getParent();
            name = new ObjectName(domain + ":type=Environment" +
                    ",resourcetype=Context,host=" + host.getName() +
                    ",context=" + cn.getDisplayName() +
                    ",name=" + environment.getName());
        }
        return name;

    }



    /**
     * Create an <code>ObjectName</code> for this
     * <code>ContextResource</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param resource The ContextResource to be named
     * @return a new object name
     * @exception MalformedObjectNameException if a name cannot be created
     */
    public static ObjectName createObjectName(String domain,
                                              ContextResource resource)
            throws MalformedObjectNameException {

        ObjectName name = null;
        String quotedResourceName = ObjectName.quote(resource.getName());
        Object container =
                resource.getNamingResources().getContainer();
        if (container instanceof Server) {
            name = new ObjectName(domain + ":type=Resource" +
                    ",resourcetype=Global,class=" + resource.getType() +
                    ",name=" + quotedResourceName);
        } else if (container instanceof Context) {
            Context context = ((Context)container);
            ContextName cn = new ContextName(context.getName(), false);
            Container host = context.getParent();
            name = new ObjectName(domain + ":type=Resource" +
                    ",resourcetype=Context,host=" + host.getName() +
                    ",context=" + cn.getDisplayName() +
                    ",class=" + resource.getType() +
                    ",name=" + quotedResourceName);
        }

        return name;

    }

    /**
     * Create, register, and return an MBean for this
     * <code>UserDatabase</code> object.
     *
     * @param userDatabase The UserDatabase to be managed
     * @return a new MBean
     * @exception Exception if an MBean cannot be created or registered
     */
    static DynamicMBean createMBean(UserDatabase userDatabase)
            throws Exception {

        if (userDatabase.isSparse()) {
            // Register a sparse database bean as well
            ManagedBean managed = registry.findManagedBean("SparseUserDatabase");
            if (managed == null) {
                Exception e = new Exception(sm.getString("mBeanUtils.noManagedBean", "SparseUserDatabase"));
                throw new MBeanException(e);
            }
            String domain = managed.getDomain();
            if (domain == null) {
                domain = mserver.getDefaultDomain();
            }
            DynamicMBean mbean = managed.createMBean(userDatabase);
            ObjectName oname = createObjectName(domain, userDatabase);
            if( mserver.isRegistered( oname ))  {
                mserver.unregisterMBean(oname);
            }
            mserver.registerMBean(mbean, oname);
        }

        String mname = createManagedName(userDatabase);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception(sm.getString("mBeanUtils.noManagedBean", mname));
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null) {
            domain = mserver.getDefaultDomain();
        }
        DynamicMBean mbean = managed.createMBean(userDatabase);
        ObjectName oname = createObjectName(domain, userDatabase);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return mbean;

    }

    /**
     * Create an <code>ObjectName</code> for this
     * <code>UserDatabase</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param userDatabase The UserDatabase to be named
     * @return a new object name
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain,
                                       UserDatabase userDatabase)
            throws MalformedObjectNameException {

        ObjectName name = null;
        name = new ObjectName(domain + ":type=UserDatabase,database=" +
                userDatabase.getId());
        return name;

    }

    /**
     * Create, register, and return an MBean for this
     * <code>Role</code> object.
     *
     * @param role The Role to be managed
     * @return a new MBean
     * @exception Exception if an MBean cannot be created or registered
     */
    static DynamicMBean createMBean(Role role)
            throws Exception {

        String mname = createManagedName(role);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception(sm.getString("mBeanUtils.noManagedBean", mname));
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null) {
            domain = mserver.getDefaultDomain();
        }
        DynamicMBean mbean = managed.createMBean(role);
        ObjectName oname = createObjectName(domain, role);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return mbean;

    }

    /**
     * Create an <code>ObjectName</code> for this
     * <code>Role</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param role The Role to be named
     * @return a new object name
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain, Role role)
            throws MalformedObjectNameException {

        ObjectName name = new ObjectName(domain + ":type=Role,rolename=" +
                ObjectName.quote(role.getRolename()) +
                ",database=" + role.getUserDatabase().getId());
        return name;
    }

    /**
     * Create, register, and return an MBean for this
     * <code>Group</code> object.
     *
     * @param group The Group to be managed
     * @return a new MBean
     * @exception Exception if an MBean cannot be created or registered
     */
    static DynamicMBean createMBean(Group group)
            throws Exception {

        String mname = createManagedName(group);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception(sm.getString("mBeanUtils.noManagedBean", mname));
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null) {
            domain = mserver.getDefaultDomain();
        }
        DynamicMBean mbean = managed.createMBean(group);
        ObjectName oname = createObjectName(domain, group);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return mbean;

    }

    /**
     * Create an <code>ObjectName</code> for this
     * <code>Group</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param group The Group to be named
     * @return a new object name
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain,
                                       Group group)
            throws MalformedObjectNameException {

        ObjectName name = null;
        name = new ObjectName(domain + ":type=Group,groupname=" +
                ObjectName.quote(group.getGroupname()) +
                ",database=" + group.getUserDatabase().getId());
        return name;

    }

    /**
     * Create, register, and return an MBean for this
     * <code>User</code> object.
     *
     * @param user The User to be managed
     * @return a new MBean
     * @exception Exception if an MBean cannot be created or registered
     */
    static DynamicMBean createMBean(User user)
            throws Exception {

        String mname = createManagedName(user);
        ManagedBean managed = registry.findManagedBean(mname);
        if (managed == null) {
            Exception e = new Exception(sm.getString("mBeanUtils.noManagedBean", mname));
            throw new MBeanException(e);
        }
        String domain = managed.getDomain();
        if (domain == null) {
            domain = mserver.getDefaultDomain();
        }
        DynamicMBean mbean = managed.createMBean(user);
        ObjectName oname = createObjectName(domain, user);
        if( mserver.isRegistered( oname ))  {
            mserver.unregisterMBean(oname);
        }
        mserver.registerMBean(mbean, oname);
        return mbean;

    }

    /**
     * Create an <code>ObjectName</code> for this
     * <code>User</code> object.
     *
     * @param domain Domain in which this name is to be created
     * @param user The User to be named
     * @return a new object name
     * @exception MalformedObjectNameException if a name cannot be created
     */
    static ObjectName createObjectName(String domain, User user)
            throws MalformedObjectNameException {

        ObjectName name = new ObjectName(domain + ":type=User,username=" +
                ObjectName.quote(user.getUsername()) +
                ",database=" + user.getUserDatabase().getId());
        return name;
    }



}
