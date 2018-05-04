package com.klinker.android.drone_pilot;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import static com.klinker.android.drone_pilot.Controller.HOST;

public class DropRequest extends StringRequest {

    public DropRequest(boolean dropped, Response.Listener<String> listener, Response.ErrorListener error) {
        super(
                Method.POST,
                String.format(
                        "%s/drop?drop=%b",
                        HOST, dropped
                ),
                listener,
                error
        );
    }

}
