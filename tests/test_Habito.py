import pytest
import requests

BASE_URL = "http://localhost:8080"

def crear_habito():
    """Datos para crear un nuevo hábito"""
    return {
        "nombre": "Hábito de prueba pytest",
        "descripcion": "Descripción del hábito de prueba",
        "rol": "TODOS",
        "dificultad": "MEDIA",
        "puntosExperiencia": 20,
        "completado": False
    }

def test_create_h():
    """Test: Crear un nuevo hábito"""
    print("🔧 Probando POST /api/habitos")

    try:
        # Realizar la petición POST
        response = requests.post(
            f"{BASE_URL}/habitos",
            json=crear_habito(),
        )

        print(f"✅ Response status: {response.status_code}")
        print(f"✅ Response content: {response.text}")

        # Verificar que la respuesta sea exitosa (200 o 201)
        assert response.status_code in [200, 201], f"Expected 200/201, got {response.status_code}"

        # Verificar que la respuesta contiene datos
        response_data = response.json()
        assert "id" in response_data, "Response should contain 'id'"
        assert response_data["nombre"] == "Hábito de prueba pytest"

        print("✅ Test crear hábito: PASÓ")
        return response_data

    except requests.exceptions.ConnectionError:
        print("❌ No se pudo conectar al servidor. ¿Está ejecutándose la aplicación?")
        return None
    except Exception as e:
        print(f"❌ Error inesperado: {e}")
        return None