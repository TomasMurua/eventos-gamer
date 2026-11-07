package com.duoc.seguridadcalidad.api.dto;

import com.duoc.seguridadcalidad.api.model.Banner;

public record BannerDto(String marca, String mensaje, String rubro) {

    public static BannerDto de(Banner b) {
        return new BannerDto(b.getMarca(), b.getMensaje(), b.getRubro());
    }
}
