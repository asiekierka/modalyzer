package pl.asie.modalyze.mcp;

public final class MCPUtils {
    private MCPUtils() {

    }

    public static String getFieldKey(String name) {
        return "F:" + name;
    }

    public static String getMethodKey(String name, String sig) {
        return "M:" + name + ":" + sig;
    }
}
