package com.udacity.vehicles;


import com.udacity.vehicles.client.maps.MapsClient;
import com.udacity.vehicles.client.prices.PriceClient;
import com.udacity.vehicles.domain.Condition;
import com.udacity.vehicles.domain.Location;
import com.udacity.vehicles.domain.car.Car;
import com.udacity.vehicles.domain.car.Details;
import com.udacity.vehicles.domain.manufacturer.Manufacturer;
import com.udacity.vehicles.service.CarService;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Collections;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
public class VehiclesApiApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JacksonTester<Car> json;

    @MockBean
    private CarService carService;

    @MockBean
    private PriceClient priceClient;

    @MockBean
    private MapsClient mapsClient;


    @Before
    public void setup() {
        Car car = testCar();
        car.setId(7L);
        given(carService.save(any())).willReturn(car);
        given(carService.findById(any())).willReturn(car);
        given(carService.list()).willReturn(Collections.singletonList(car));
    }

    private Car testCar () {
        Car car = new Car();
        car.setLocation(new Location(77.777777, -77.777777));
        Details details = new Details();
        Manufacturer manufacturer = new Manufacturer(101, "Chevrolet");
        details.setManufacturer(manufacturer);
        details.setModel("Impala");
        details.setMileage(777777);
        details.setExternalColor("white");
        details.setBody("sedan");
        details.setEngine("6.6L V8");
        details.setFuelType("Diesel");
        details.setModelYear(2022);
        details.setProductionYear(2021);
        details.setNumberOfDoors(5);
        car.setDetails(details);
        car.setCondition(Condition.USED);
        car.setPrice(this.priceClient.getPrice(car.getId()));
        car.setLocation(this.mapsClient.getAddress(car.getLocation()));
        return car;
    }


    @Test
    public void createCar() throws Exception {
        Car car = testCar();
        mvc.perform(
                        post(new URI("/cars"))
                                .content(json.write(car).getJson())
                                .contentType(MediaType.APPLICATION_JSON_UTF8)
                                .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isCreated());
    }

    @Test
    public void listCars() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/cars").contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.carList.length()", is(1)))
                .andExpect(jsonPath("$._embedded.carList[0].details.model", is("Impala")))
                .andExpect(jsonPath("$._embedded.carList[0]._links.self.href", is("http://localhost/cars/7")));
    }

    @Test
    public void findCar() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/cars/7").contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.condition", is("USED")))
                .andExpect(jsonPath("$.details.model", is("Impala")))
                .andExpect(jsonPath("$._links.self.href", is("http://localhost/cars/7")));
    }

    @Test
    public void testGetPrice() throws Exception {
        mvc.perform(get("/services/price").param("vehicleId", "7"))
                .andExpect(status().isOk());
    }

    @Test
    public void updateCar() throws Exception {
        Car newCar = testCar();
        newCar.setId(7L);;
        newCar.setCondition(Condition.NEW);
        Details newDetails = newCar.getDetails();
        newDetails.setModel("Musclecar");
        newCar.setDetails(newDetails);
        given(carService.save(any())).willReturn(newCar);
        mvc.perform(
                        put("/cars/{id}",7)
                                .content(new ObjectMapper().writeValueAsString(newCar))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.condition").value("NEW"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.details.model").value("Musclecar"));
    }


    @Test
    public void deleteCar() throws Exception {
        mvc.perform(MockMvcRequestBuilders.delete("/cars/{id}", 7))
                .andExpect(status().isOk());
        verify(carService, times(1)).delete(7L);
    }

    }
