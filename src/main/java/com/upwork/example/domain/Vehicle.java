package com.upwork.example.domain;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "vehicles")
@Cacheable(false)
@NamedQueries({
	@NamedQuery(name = "Vehicle.findAll",
			query = "SELECT c FROM Vehicle c"),
	@NamedQuery(name = "Vehicle.getVehicle",
	query = "SELECT c FROM Vehicle c WHERE c.id = :id"),
	@NamedQuery(name = "Vehicle.editVehicle",
	query = "UPDATE Vehicle c SET c.description = :desc WHERE c.id = :id")
})
public class Vehicle {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private int id;

    private String item;

    private String description;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
