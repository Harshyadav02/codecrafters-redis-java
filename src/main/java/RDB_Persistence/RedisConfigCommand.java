package RDB_Persistence;

public class RedisConfigCommand {
    private final String dir;
    private final String dbFileName;

    public RedisConfigCommand(String dir, String dbFileName) {
        // Default values (can be replaced with actual configurable values)
        this.dir = dir;
        this.dbFileName = dbFileName;
    }

    public String getDir() {
        // RESP format response for CONFIG GET dir
        return "*2\r\n$3\r\ndir\r\n$" + dir.length() + "\r\n" + dir + "\r\n";
    }

    public String getDbFileName() {
        // RESP format response for CONFIG GET dbfilename
        return "*2\r\n$9\r\ndbfilename\r\n$" + dbFileName.length() + "\r\n" + dbFileName + "\r\n";
    }
}
