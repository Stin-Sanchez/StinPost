package com.stinjoss.springbootmvc.app.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stinjoss.springbootmvc.app.domain.entities.Clients;
import com.stinjoss.springbootmvc.app.domain.entities.Products;
import com.stinjoss.springbootmvc.app.domain.entities.enums.StatesProducts;
import com.stinjoss.springbootmvc.app.repositories.ClientRepository;
import com.stinjoss.springbootmvc.app.repositories.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;

    public DataSeeder(ClientRepository clientRepository, ProductRepository productRepository) {
        this.clientRepository = clientRepository;
        this.productRepository = productRepository;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Iniciando DataSeeder...");
        
        long countClients = clientRepository.count();
        System.out.println("📊 Clientes actuales en BD: " + countClients);

        if (countClients == 0) {
            seedClients();
        } else {
            System.out.println("⚠️ OMITIDO: Ya existen clientes en la base de datos.");
        }

        long countProducts = productRepository.count();
        System.out.println("📊 Productos actuales en BD: " + countProducts);

        if (countProducts == 0) {
            seedProducts();
        } else {
            System.out.println("⚠️ OMITIDO: Ya existen productos en la base de datos.");
        }
    }

    private void seedClients() {
        System.out.println("🌱 Intentando descargar Clientes desde RandomUser.me...");
        String url = "https://randomuser.me/api/?results=500&nat=es&inc=name,email,cell,dob,location,id";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                System.err.println("❌ La API respondió con error: " + response.getStatusCode());
                return;
            }

            String jsonResponse = response.getBody();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            JsonNode results = root.path("results");

            List<Clients> lista = new ArrayList<>();
            Set<String> emailsProcesados = new HashSet<>(); // Para evitar duplicados en el mismo lote
            Random random = new Random();

            for (JsonNode node : results) {
                String email = node.path("email").asText();
                
                if (emailsProcesados.contains(email)) {
                    continue; 
                }
                emailsProcesados.add(email);

                Clients c = new Clients();
                c.setName(node.path("name").path("first").asText());
                c.setLastname(node.path("name").path("last").asText());
                c.setEmail(email);
                
                String rawPhone = node.path("cell").asText();
                String cleanPhone = rawPhone.replaceAll("[^0-9]", ""); 
                if (cleanPhone.length() < 8) cleanPhone = "09" + (100000 + random.nextInt(900000));
                if (cleanPhone.length() > 15) cleanPhone = cleanPhone.substring(0, 15);
                c.setCellPhone(cleanPhone);

                long dniNum = 1000000000L + (long)(random.nextDouble() * 900000000L);
                c.setDni(String.valueOf(dniNum));

                c.setAge((byte) node.path("dob").path("age").asInt());
                
                String calle = node.path("location").path("street").path("name").asText();
                String numero = node.path("location").path("street").path("number").asText();
                c.setDirection(calle + " " + numero);

                c.setActive(true);
                c.setCreatedAt(LocalDateTime.now()); 

                lista.add(c);
            }

            if (!lista.isEmpty()) {
                int guardados = 0;
                for (Clients cliente : lista) {
                    try {
                        clientRepository.save(cliente);
                        guardados++;
                    } catch (DataIntegrityViolationException e) {
                        System.err.println("⚠️ Email duplicado omitido: " + cliente.getEmail());
                    } catch (Exception e) {
                        System.err.println("⚠️ Error guardando cliente: " + e.getMessage());
                    }
                }
                System.out.println("✅ ÉXITO: " + guardados + " Clientes guardados.");
            } 

        } catch (Exception e) {
            System.err.println("❌ EXCEPCIÓN en seedClients: " + e.getMessage());
        }
    }

    private void seedProducts() {
        System.out.println("🌱 Intentando descargar Productos desde Makeup API...");
        String url = "http://makeup-api.herokuapp.com/api/v1/products.json";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            String jsonResponse = response.getBody();
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            List<Products> lista = new ArrayList<>();
            Random random = new Random();

            int count = 0;
            for (JsonNode node : root) {
                if (count >= 500) break;

                Products p = new Products();
                p.setNameProducto(node.path("name").asText());

                String desc = node.path("description").asText();
                if (desc.length() > 200) desc = desc.substring(0, 197) + "...";
                p.setDescription(desc.isEmpty() ? "Sin descripción" : desc);

                p.setMarca(node.path("brand").asText("Genérico"));
                p.setCode("PROD-" + node.path("id").asText());

                double priceVal = node.path("price").asDouble();
                if (priceVal <= 0) priceVal = 10.0 + random.nextDouble() * 50.0;
                p.setPrice(BigDecimal.valueOf(priceVal));

                int stock = random.nextInt(100);
                p.setStock(stock);
                p.setMinStock(5);

                if (stock == 0) p.setState(StatesProducts.AGOTADO);
                else if (stock <= 5) p.setState(StatesProducts.CON_STOCK_MINIMO);
                else p.setState(StatesProducts.DISPONIBLE);

                String imageUrl = node.path("image_link").asText();
                if (imageUrl != null && imageUrl.length() > 255) {
                    imageUrl = null; 
                }
                p.setImage(imageUrl);

                p.setActive(true);
                p.setCreatedAt(LocalDateTime.now()); // Añadido para BaseEntity

                lista.add(p);
                count++;
            }

            productRepository.saveAll(lista);
            System.out.println("✅ ÉXITO: " + lista.size() + " Productos guardados.");
            
            // LOG DE VERIFICACIÓN
            long finalCount = productRepository.count();
            System.out.println("📊 Verificación final: " + finalCount + " productos en la BD.");

        } catch (Exception e) {
            System.err.println("❌ EXCEPCIÓN en seedProducts: " + e.getMessage());
        }
    }
}
