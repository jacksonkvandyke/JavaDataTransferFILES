class DataSizeToReadable {

    long data = 0;

    //This class converts data in number of bytes to the best readable format
    DataSizeToReadable(long data){
        this.data = data;
        Convert();
    }

    String Convert(){
        String totalString = "";

        //KB
        if ((data / 1024) > 1){
            totalString = String.format("%d KB", data / 1024);
        }

        //MB
        if ((data / Math.pow(1024, 2)) > 1){
            totalString = String.format("%d MB", data / Math.pow(1024, 2));
        }

        //GB
        if ((data / Math.pow(1024, 3)) > 1){
            totalString = String.format("%d GB", data / Math.pow(1024, 3));
        }

        //TB
        if ((data / Math.pow(1024, 4)) > 1){
            totalString = String.format("%d TB", data / Math.pow(1024, 4));
        }

        //Return final string
        return totalString;
    }

}