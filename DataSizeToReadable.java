class DataSizeToReadable {

    String Convert(long data){
        String totalString = "";

        //KB
        if ((data / 1024) > 1){
            totalString = String.format("%s KB", String.valueOf(Math.round((double) data / 1024 * 100) / 100));
        }

        //MB
        if ((data / Math.pow(1024, 2)) > 1){
            totalString = String.format("%s MB", String.valueOf(Math.round((double) data / Math.pow(1024, 2) * 100) / 100));
        }

        //GB
        if ((data / Math.pow(1024, 3)) > 1){
            totalString = String.format("%s GB", String.valueOf(Math.round((double) data / Math.pow(1024, 3) * 100) / 100));
        }

        //TB
        if ((data / Math.pow(1024, 4)) > 1){
            totalString = String.format("%s TB", String.valueOf(Math.round((double) data / Math.pow(1024, 4) * 100) / 100));
        }

        //Return final string
        return totalString;
    }

}