package com.mx.mbrl.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"subcategories", "parentCategory", "hibernateLazyInitializer", "handler"})
public class Category {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@ManyToOne
	@JoinColumn(name = "parent_category_id")
	private Category parentCategory;

	@OneToMany(mappedBy = "parentCategory")
	private List<Category> subcategories;

	@CreationTimestamp
	@Column(name = "created_at", columnDefinition = "TIMESTAMP")
	private LocalDateTime createdAt;
}

