package com.mx.mbrl.service;

import com.mx.mbrl.dto.CustomerRequestDTO;
import com.mx.mbrl.entity.Customer;
import com.mx.mbrl.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

	private final CustomerRepository customerRepository;

	@Transactional
	public Customer create(CustomerRequestDTO customerRequestDTO) {
		log.info("Creando nuevo cliente: {}", customerRequestDTO.getName());

		// Validar que el email no esté duplicado si se proporciona
		if (customerRequestDTO.getEmail() != null && !customerRequestDTO.getEmail().isEmpty()) {
			if (customerRepository.findByEmail(customerRequestDTO.getEmail()).isPresent()) {
				throw new IllegalArgumentException("Ya existe un cliente con el email: " + customerRequestDTO.getEmail());
			}
		}

		// Validar que el teléfono no esté duplicado si se proporciona
		if (customerRequestDTO.getPhone() != null && !customerRequestDTO.getPhone().isEmpty()) {
			if (customerRepository.findByPhone(customerRequestDTO.getPhone()).isPresent()) {
				throw new IllegalArgumentException("Ya existe un cliente con el teléfono: " + customerRequestDTO.getPhone());
			}
		}

		// Crear cliente
		Customer customer = new Customer();
		customer.setName(customerRequestDTO.getName());
		customer.setEmail(customerRequestDTO.getEmail());
		customer.setPhone(customerRequestDTO.getPhone());
		customer.setAddress(customerRequestDTO.getAddress());
		customer.setCity(customerRequestDTO.getCity());
		customer.setState(customerRequestDTO.getState());
		customer.setPostalCode(customerRequestDTO.getPostalCode());
		customer.setTotalOrders(0);
		customer.setTotalSpent(java.math.BigDecimal.ZERO);
		customer.setCreatedAt(LocalDateTime.now());

		Customer savedCustomer = customerRepository.save(customer);
		log.info("Cliente creado con ID: {}", savedCustomer.getId());

		return savedCustomer;
	}

	@Transactional
	public Customer update(Long customerId, CustomerRequestDTO customerRequestDTO) {
		log.info("Actualizando cliente con ID: {}", customerId);

		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + customerId));

		// Validar email único si se actualiza
		if (customerRequestDTO.getEmail() != null && !customerRequestDTO.getEmail().isEmpty()) {
			if (!customer.getEmail().equals(customerRequestDTO.getEmail())) {
				if (customerRepository.findByEmail(customerRequestDTO.getEmail()).isPresent()) {
					throw new IllegalArgumentException("Ya existe un cliente con el email: " + customerRequestDTO.getEmail());
				}
			}
		}

		// Validar teléfono único si se actualiza
		if (customerRequestDTO.getPhone() != null && !customerRequestDTO.getPhone().isEmpty()) {
			if (!customer.getPhone().equals(customerRequestDTO.getPhone())) {
				if (customerRepository.findByPhone(customerRequestDTO.getPhone()).isPresent()) {
					throw new IllegalArgumentException("Ya existe un cliente con el teléfono: " + customerRequestDTO.getPhone());
				}
			}
		}

		// Actualizar campos
		customer.setName(customerRequestDTO.getName());
		customer.setEmail(customerRequestDTO.getEmail());
		customer.setPhone(customerRequestDTO.getPhone());
		customer.setAddress(customerRequestDTO.getAddress());
		customer.setCity(customerRequestDTO.getCity());
		customer.setState(customerRequestDTO.getState());
		customer.setPostalCode(customerRequestDTO.getPostalCode());

		Customer updatedCustomer = customerRepository.save(customer);
		log.info("Cliente actualizado con ID: {}", updatedCustomer.getId());

		return updatedCustomer;
	}

	@Transactional(readOnly = true)
	public Customer findById(Long customerId) {
		log.debug("Buscando cliente con ID: {}", customerId);

		return customerRepository.findById(customerId)
				.orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + customerId));
	}

	@Transactional(readOnly = true)
	public List<Customer> findAll() {
		log.debug("Obteniendo todos los clientes");

		return customerRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Customer getCustomerStats(Long customerId) {
		log.info("Obteniendo estadísticas del cliente ID: {}", customerId);

		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + customerId));

		// Las estadísticas ya están en la entidad:
		// - totalOrders
		// - totalSpent
		// - lastOrderDate
		// Se actualizan automáticamente cuando se crea/actualiza/cancela una orden

		log.debug("Estadísticas del cliente {} - Órdenes: {}, Total gastado: {}, Última compra: {}",
				customerId, customer.getTotalOrders(), customer.getTotalSpent(), customer.getLastOrderDate());

		return customer;
	}

	@Transactional(readOnly = true)
	public Customer findByEmail(String email) {
		log.debug("Buscando cliente por email: {}", email);

		return customerRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con email: " + email));
	}

	@Transactional(readOnly = true)
	public Customer findByPhone(String phone) {
		log.debug("Buscando cliente por teléfono: {}", phone);

		return customerRepository.findByPhone(phone)
				.orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con teléfono: " + phone));
	}

	@Transactional
	public void delete(Long customerId) {
		log.info("Eliminando cliente con ID: {}", customerId);

		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + customerId));

		// Validar que no tenga órdenes activas
		if (customer.getTotalOrders() > 0) {
			log.warn("No se puede eliminar cliente con órdenes activas. ID: {}, Total órdenes: {}", 
					customerId, customer.getTotalOrders());
			throw new IllegalArgumentException("No se puede eliminar un cliente que tiene órdenes asociadas");
		}

		customerRepository.deleteById(customerId);
		log.info("Cliente eliminado exitosamente: {}", customerId);
	}

	@Transactional(readOnly = true)
	public Long getCustomerCount() {
		log.debug("Obteniendo cantidad total de clientes");

		return customerRepository.count();
	}

	@Transactional(readOnly = true)
	public java.math.BigDecimal getTotalSalesAmount() {
		log.debug("Calculando ventas totales de todos los clientes");

		// Se puede mejorar con una query específica que suma totalSpent
		List<Customer> customers = customerRepository.findAll();
		return customers.stream()
				.map(Customer::getTotalSpent)
				.reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
	}
}


