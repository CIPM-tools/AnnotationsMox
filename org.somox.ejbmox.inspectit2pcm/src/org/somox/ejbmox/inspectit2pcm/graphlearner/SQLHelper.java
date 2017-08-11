package org.somox.ejbmox.inspectit2pcm.graphlearner;

public class SQLHelper {

    public static boolean isActualStatement(String sql) {
        if (sql.startsWith("/*") || sql.toLowerCase().startsWith("select @@")) {
            return false;
        }
        return true;
    }
    
}
