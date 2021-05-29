package org.cloudbus.cloudsim;

public class CuckooCloudlet extends Cloudlet {
    final Boolean Cuckoo;
    final int CuckooNo;
    public CuckooCloudlet(final int cloudletId, final long cloudletLength, final int pesNumber, final long cloudletFileSize,
            final long cloudletOutputSize, final UtilizationModel utilizationModelCpu,
            final UtilizationModel utilizationModelRam, final UtilizationModel utilizationModelBw, final Boolean Cuckoo, final int CuckooNo) {
                
                super(cloudletId, cloudletLength, pesNumber, cloudletFileSize, cloudletOutputSize, utilizationModelCpu,
             utilizationModelRam,
             utilizationModelBw);
             this.Cuckoo=Cuckoo;
             this.CuckooNo = CuckooNo;
    }
    
    /**
     * Gets the user or owner ID of this Cloudlet.
     *
     * @return the user ID or <tt>-1</tt> if the user ID has not been set before
     * @pre $none
     * @post $result >= -1
     */
    public Boolean isCuckoo() {
        return Cuckoo;
    }
    
    public int getJobNumber() {
        return CuckooNo;
    }
}
