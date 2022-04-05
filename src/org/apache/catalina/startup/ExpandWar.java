package org.apache.catalina.startup;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.res.StringManager;

import java.io.File;

public class ExpandWar {
    private static final Log log = LogFactory.getLog(ExpandWar.class);

    /**
     * The string resources for this package.
     */
    protected static final StringManager sm =
            StringManager.getManager(Constants.Package);



    /**
     * Delete the specified directory, including all of its contents and
     * sub-directories recursively. Any failure will be logged.
     *
     * @param dir File object representing the directory to be deleted
     * @return <code>true</code> if the deletion was successful
     */
    public static boolean delete(File dir) {
        // Log failure by default
        return delete(dir, true);
    }


    /**
     * Delete the specified directory, including all of its contents and
     * sub-directories recursively.
     *
     * @param dir File object representing the directory to be deleted
     * @param logFailure <code>true</code> if failure to delete the resource
     *                   should be logged
     * @return <code>true</code> if the deletion was successful
     */
    public static boolean delete(File dir, boolean logFailure) {
        boolean result;
        if (dir.isDirectory()) {
            result = deleteDir(dir, logFailure);
        } else {
            if (dir.exists()) {
                result = dir.delete();
            } else {
                result = true;
            }
        }
        if (logFailure && !result) {
            log.error(sm.getString(
                    "expandWar.deleteFailed", dir.getAbsolutePath()));
        }
        return result;
    }

    /**
     * Delete the specified directory, including all of its contents and
     * sub-directories recursively.
     *
     * @param dir File object representing the directory to be deleted
     * @param logFailure <code>true</code> if failure to delete the resource
     *                   should be logged
     * @return <code>true</code> if the deletion was successful
     */
    public static boolean deleteDir(File dir, boolean logFailure) {

        String files[] = dir.list();
        if (files == null) {
            files = new String[0];
        }
        for (String s : files) {
            File file = new File(dir, s);
            if (file.isDirectory()) {
                deleteDir(file, logFailure);
            } else {
                file.delete();
            }
        }

        boolean result;
        if (dir.exists()) {
            result = dir.delete();
        } else {
            result = true;
        }

        if (logFailure && !result) {
            log.error(sm.getString(
                    "expandWar.deleteFailed", dir.getAbsolutePath()));
        }

        return result;
    }



}
