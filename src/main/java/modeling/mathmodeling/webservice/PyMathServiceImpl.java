package modeling.mathmodeling.webservice;

import modeling.mathmodeling.network.PyServer;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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
}
