package pizza.kkomdae.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;

@Getter
public class RentResults {
    private final int studentNum;
    private final String studentName;
    private final String studentRegion;
    private final int studentClassNum;
    private final String deviceType;
    private final String modelCode;
    private final String serialNum;
    private final boolean release;

    @QueryProjection
    public RentResults(int studentNum, String studentName, String studentRegion, int studentClassNum, String deviceType
                       ,String laptopModelCode, String laptopSerialNum, String phoneModelCode,String phoneSerialNum, boolean release
    ) {
        this.studentNum = studentNum;
        this.studentName = studentName;
        this.studentRegion = studentRegion;
        this.studentClassNum = studentClassNum;
        this.deviceType = deviceType;
        if(deviceType.equals("Laptop")){
            this.modelCode = laptopModelCode;
            this.serialNum = laptopSerialNum;
        }else{
            this.modelCode = phoneModelCode;
            this.serialNum = phoneSerialNum;
        }
        this.release = release;

    }
}
