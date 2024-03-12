class DataSizeToReadable {

    String Convert(long data){
        String totalString = "";

        //KB
        if ((data / 1024) > 1){
            totalString = String.format("%.2f KB", (long) (data / 1024));
        }

        //MB
        if ((data / Math.pow(1024, 2)) > 1){
            totalString = String.format("%.2f MB", (long) (data / Math.pow(1024, 2)));
        }

        //GB
        if ((data / Math.pow(1024, 3)) > 1){
            totalString = String.format("%.2f GB", (long) (data / Math.pow(1024, 3)));
        }

        //TB
        if ((data / Math.pow(1024, 4)) > 1){
            totalString = String.format("%.2f TB", (long) (data / Math.pow(1024, 4)));
        }

        //Return final string
        return totalString;
    }

}