package kakaopay.money.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import kakaopay.money.dto.response.ErrorMessage;
import kakaopay.money.dto.response.ResponseError;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;

@Service
public class TestService {

    @Autowired private ObjectMapper objectMapper;

    @Autowired private ModelMapper modelMapper;

    public boolean isSuccess(MvcResult result) throws Exception {
        Map<String, Object> map = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        return (Boolean) map.get("success");
    }

    public boolean isBadRequest(MvcResult result) throws Exception {
        ResponseError error = getResponseError(result);
        return error.getStatus() == HttpStatus.BAD_REQUEST.value();
    }

    public boolean isNotAcceptable(MvcResult result) throws Exception {
        ResponseError error = getResponseError(result);
        return error.getStatus() == HttpStatus.NOT_ACCEPTABLE.value();
    }

    public boolean isErrorMessage(MvcResult result, ErrorMessage message) throws Exception {
        ResponseError error = getResponseError(result);
        return error.getMessage().equals(message.getValue());
    }

    public ResponseError getResponseError(MvcResult result) throws Exception {
        Map<String, Object> map = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        return modelMapper.map(map.get("error"), ResponseError.class);
    }

    public Object getResponse(MvcResult result) throws Exception{
        HashMap<String, Object> map = objectMapper.readValue(result.getResponse().getContentAsString(), HashMap.class);
        return map.get("response");
    }
}
