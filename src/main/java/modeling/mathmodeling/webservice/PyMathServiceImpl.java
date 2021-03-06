package modeling.mathmodeling.webservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import modeling.mathmodeling.network.PyServer;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class PyMathServiceImpl implements PyMathService {

    @Override
    public String expand(String input) {
        String output = "";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.HOURS)
                .readTimeout(1, TimeUnit.HOURS)
                .writeTimeout(1, TimeUnit.HOURS)
                .build();
        input = StringUtils.replace(input, " ", "");

        RequestBody formBody = new FormBody.Builder()
                .add("input", input)
                .build();

        Request request = new Request.Builder()
                .url(PyServer.getURL() + "/expand")
                .post(formBody)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            output = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    @Override
    public String expand(List<String> input) {
        return null;
    }

    @Override
    public HashMap<String, Double> expandToTerms(String input) {
        HashMap<String, Double> output = null;
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.HOURS)
                .readTimeout(1, TimeUnit.HOURS)
                .writeTimeout(1, TimeUnit.HOURS)
                .build();
        input = StringUtils.replace(input, " ", "");

        RequestBody formBody = new FormBody.Builder()
                .add("input", input)
                .build();

        Request request = new Request.Builder()
                .url(PyServer.getURL() + "/expand-list")
                .post(formBody)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            ObjectMapper mapper = new ObjectMapper();
            output = mapper.readValue(response.body().string(), HashMap.class);
            output.remove("0");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    @Override
    public String d(String input, String diffVar) {
        String output = "";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.HOURS)
                .readTimeout(1, TimeUnit.HOURS)
                .writeTimeout(1, TimeUnit.HOURS)
                .build();
        input = StringUtils.replace(input, " ", "");

        RequestBody formBody = new FormBody.Builder()
                .add("input", input)
                .add("variable", diffVar)
                .build();

        Request request = new Request.Builder()
                .url(PyServer.getURL() + "/d")
                .post(formBody)
                .build();

        Call call = client.newCall(request);
        try {
            Response response = call.execute();
            output = response.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }
}
