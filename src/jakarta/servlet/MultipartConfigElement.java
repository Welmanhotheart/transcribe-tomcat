package jakarta.servlet;

public class MultipartConfigElement {

    private final String location;// = "";
    private final long maxFileSize;// = -1;
    private final long maxRequestSize;// = -1;
    private final int fileSizeThreshold;// = 0;

    /**
     * Create a programmatic multi-part configuration from the individual
     * configuration elements.
     *
     * @param location          The temporary location to store files
     * @param maxFileSize       The maximum permitted size for a single file
     * @param maxRequestSize    The maximum permitted size for a request
     * @param fileSizeThreshold The size above which the file is save in the
     *                              temporary location rather than retained in
     *                              memory.
     */
    public MultipartConfigElement(String location, long maxFileSize,
                                  long maxRequestSize, int fileSizeThreshold) {
        // Keep empty string default if location is null
        if (location != null) {
            this.location = location;
        } else {
            this.location = "";
        }
        this.maxFileSize = maxFileSize;
        this.maxRequestSize = maxRequestSize;
        // Avoid threshold values of less than zero as they cause trigger NPEs
        // in the Commons FileUpload port for fields that have no data.
        if (fileSizeThreshold > 0) {
            this.fileSizeThreshold = fileSizeThreshold;
        } else {
            this.fileSizeThreshold = 0;
        }
    }


}
