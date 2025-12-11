INSERT INTO usuarios (username, password, email, rol, activo, experiencia_total, experiencia_actual, nivel, experiencia_para_siguiente_nivel)
SELECT 'admin', 'jkbBaXoTlWn4Qxa2cJ36lQaeXeXMxnAy8V06mXHLr//0qSt1FnIo3zyHoX26D9fY', 'admin@elitemacro.com', 'ROLE_ADMIN', true, 5000, 1200, 25, 2000
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'admin');