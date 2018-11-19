import org.apache.commons.lang3.ArrayUtils;
import org.bytedeco.javacpp.Pointer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.nd4j.linalg.api.buffer.DataBuffer;
import org.nd4j.linalg.api.buffer.util.DataTypeUtil;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.DoublePointer;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.nativeblas.NativeOps;
import org.nd4j.nativeblas.NativeOpsHolder;

import java.util.ArrayList;


public class NumpyArray {
    enum DType{
        FLOAT32,
        FLOAT64
    }
    private static NativeOps nativeOps = NativeOpsHolder.getInstance().getDeviceNativeOps();
    private long address;
    private long[] shape;
    private long[] strides;
    private DType dtype = DType.FLOAT32;
    private INDArray nd4jArray;

    public NumpyArray(long address, long[] shape, long strides[]){
        this.address = address;
        this.shape = shape;
        this.strides = strides;
        setND4JArray();
    }
    public NumpyArray(long address, long[] shape, long strides[], DType dtype) throws Exception{
        this.address = address;
        this.shape = shape;
        this.strides = strides;
        this.dtype = dtype;
        setND4JArray();
    }

    public long getAddress() {
        return address;
    }

    public long[] getShape() {
        return shape;
    }

    public long[] getStrides() {
        return strides;
    }

    public DType getDType() {
        return dtype;
    }

    public JSONObject toJSON(){
        JSONObject jsonObject = new JSONObject();
        JSONArray data = new JSONArray();
        long size = 1;
        for (long d: nd4jArray.shape()){
            size *= d;
        }
        for (long i=0; i<size; i++){
            data.add(nd4jArray.getDouble(i));
        }

        JSONArray shape = new JSONArray();
        for (long d: this.shape){
            shape.add(d);
        }
        String dtypeStr;
        if (dtype == DType.FLOAT32){
            dtypeStr = "float32";
        }
        else{
            dtypeStr = "float64";
        }
        jsonObject.put("data", data);
        jsonObject.put("shape", shape);
        jsonObject.put("dtype", dtypeStr);
        return jsonObject;
    }


    public NumpyArray(JSONObject json){
        address = (Long)json.get("address");
        JSONArray shapeJson = (JSONArray)json.get("shape");
        shape = new long[shapeJson.size()];
        for (int i=0; i<shape.length; i++){
            shape[i] = (Long)shapeJson.get(i);
        }
        JSONArray stridesJson = (JSONArray)json.get("strides");
        strides = new long[stridesJson.size()];
        for (int i=0; i<strides.length; i++){
            strides[i] = (Long)stridesJson.get(i);
        }
        String dtpeStr = (String)json.get("dtype");
        if (dtpeStr != null){
            if (dtpeStr.equals("float32")){
                dtype = DType.FLOAT32;
            }
            else{
                dtype = DType.FLOAT64;
            }
        }
        setND4JArray();
    }

    public INDArray getND4JArray() {
        return nd4jArray;
    }

    private void setND4JArray(){
            long size = 1;
            for(long d: shape){
                size *= d;
            }
            Pointer ptr = nativeOps.pointerForAddress(address);
            DataBuffer buff;
            if (dtype == DType.FLOAT32) {
                FloatPointer floatPtr = new FloatPointer(ptr);
                buff = Nd4j.createBuffer(floatPtr, size);
            }
            else{
                DoublePointer doublePtr = new DoublePointer(ptr);
                buff = Nd4j.createBuffer(doublePtr, size);
            }
            int elemSize = buff.getElementSize();
            long[] nd4jStrides = new long[strides.length];
            for (int i=0; i<strides.length; i++){
                nd4jStrides[i] = strides[i] / elemSize;
            }
            nd4jArray = Nd4j.create(buff, shape, nd4jStrides, 0);
    }

    public NumpyArray(INDArray nd4jArray){
        DataBuffer buff = nd4jArray.data();
        address = buff.pointer().address();
        shape = nd4jArray.shape();
        long[] nd4jStrides = nd4jArray.stride();
        strides = new long[nd4jStrides.length];
        int elemSize = buff.getElementSize();
        for(int i=0; i<strides.length; i++){
            strides[i] = nd4jStrides[i] * elemSize;
        }
        String jDtype = DataTypeUtil.getDTypeForName(DataTypeUtil.getDtypeFromContext());
        if (jDtype.equals("float")){
            dtype = DType.FLOAT32;
        }
        else if (jDtype.equals("double")){
            dtype = DType.FLOAT64;
        }
        else{
            //throw new Exception("Unsupported context dtype: " + jDtype);
        }
        this.nd4jArray = nd4jArray;
    }

}
