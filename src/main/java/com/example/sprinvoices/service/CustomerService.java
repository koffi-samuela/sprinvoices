package com.example.sprinvoices.service;

import com.example.sprinvoices.models.Customer;
import com.example.sprinvoices.models.Role;
import com.example.sprinvoices.models.UserAccount;
import com.example.sprinvoices.repository.CustomerRepository;
import com.example.sprinvoices.repository.RoleRepository;
import com.example.sprinvoices.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Récupère tous les clients
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    // Récupère un client par son id
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client introuvable : " + id));
    }

    // Crée un client ET son compte utilisateur en même temps
    @Transactional
    public void create(Customer customer, String username, String password, String roleName) {

        // verifie que le username n'est pas déjà pris
        if (userAccountRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Nom d'utilisateur déjà pris : " + username);
        }
        // 1. Récupère le rôle CLIENT
        Role role = roleRepository.findByName(roleName);
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPassword(passwordEncoder.encode(password));
        account.setEnabled(true);
        account.setRole(role);
        userAccountRepository.save(account);
        customer.setUserAccount(account);
        customerRepository.save(customer);
    }

    // Met à jour un client existant
    @Transactional
    public void update(Long id, Customer updated) {
        Customer existing = findById(id);
        existing.setName(updated.getName());
        existing.setCorporateName(updated.getCorporateName());
        existing.setAddress(updated.getAddress());
        existing.setZipcode(updated.getZipcode());
        existing.setCity(updated.getCity());
        existing.setDelay(updated.getDelay());
        customerRepository.save(existing);
    }

    // Supprime un client et son compte utilisateur
    @Transactional
    public void delete(Long id) {
        Customer customer = findById(id);
        customerRepository.delete(customer);
        if (customer.getUserAccount() != null) {
            userAccountRepository.delete(customer.getUserAccount());
        }
    }
}