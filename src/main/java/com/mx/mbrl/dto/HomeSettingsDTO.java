package com.mx.mbrl.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeSettingsDTO {
	private List<String> banners;
	private List<String> gallery;
	private List<TestimonialDTO> testimonials;
}
