package com.klinker.android.drone_pilot;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import static com.klinker.android.drone_pilot.Controller.HOST;

public class ControlRequest extends StringRequest {

    public ControlRequest(double strafeX, double strafeY, double angle, double lift, Response.Listener<String> listener, Response.ErrorListener error) {
        super(
                Method.POST,
                String.format(
                        "%s/controller?strafeX=%f&strafeY=%f&lift=%f&angle=%f",
                        HOST, strafeX, strafeY, lift, angle
                ),
                listener,
                error
        );
    }

}
