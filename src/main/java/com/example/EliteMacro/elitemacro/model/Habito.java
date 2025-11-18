package com.example.EliteMacro.elitemacro.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "habito")
public class Habito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SOLUCIÓN: Cambiar a EAGER para evitar LazyInitializationException
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "rol", length = 50)
    private String rol;

    @Column(name = "completado", nullable = false)
    private boolean completado = false;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "dificultad", length = 20)
    private String dificultad;

    @Column(name = "frecuencia", length = 50)
    private String frecuencia;

    @Column(name = "puntos_experiencia")
    private Integer puntosExperiencia = 0;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    // Constructores, getters, setters y otros métodos permanecen igual...
    public Habito() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Habito(Usuario usuario, String nombre, String descripcion, String rol) {
        this();
        this.usuario = usuario;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.rol = rol;
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) {
        this.completado = completado;
        if (completado) {
            this.fechaActualizacion = LocalDateTime.now();
        }
    }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public String getDificultad() { return dificultad; }
    public void setDificultad(String dificultad) { this.dificultad = dificultad; }

    public String getFrecuencia() { return frecuencia; }
    public void setFrecuencia(String frecuencia) { this.frecuencia = frecuencia; }

    public Integer getPuntosExperiencia() { return puntosExperiencia; }
    public void setPuntosExperiencia(Integer puntosExperiencia) { this.puntosExperiencia = puntosExperiencia; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    // Métodos de utilidad
    public void marcarComoCompletado() {
        this.completado = true;
        this.fechaActualizacion = LocalDateTime.now();
        calcularPuntosExperiencia();
    }

    public void reiniciarHabito() {
        this.completado = false;
        this.fechaActualizacion = LocalDateTime.now();
    }

    private void calcularPuntosExperiencia() {
        int puntosBase = 10;
        switch (dificultad != null ? dificultad.toUpperCase() : "FACIL") {
            case "MEDIO":
                puntosBase = 20;
                break;
            case "DIFICIL":
                puntosBase = 30;
                break;
        }
        this.puntosExperiencia = puntosBase;
    }

    @Override
    public String toString() {
        return "Habito{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", completado=" + completado +
                ", rol='" + rol + '\'' +
                '}';
    }
}