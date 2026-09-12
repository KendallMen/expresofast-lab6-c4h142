USE [ExpresoFast_C4H142_II2026];
GO

IF OBJECT_ID(N'dbo.EmpresaLogistica', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.EmpresaLogistica (
        empresa_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_EmpresaLogistica PRIMARY KEY,
        nombre VARCHAR(100) NOT NULL CONSTRAINT UQ_EmpresaLogistica_nombre UNIQUE,
        cedula_juridica VARCHAR(20) NOT NULL CONSTRAINT UQ_EmpresaLogistica_cedula UNIQUE,
        telefono VARCHAR(20) NOT NULL,
        fecha_registro DATETIME2 NOT NULL CONSTRAINT DF_EmpresaLogistica_fecha DEFAULT SYSDATETIME()
    );
END;
GO

IF OBJECT_ID(N'dbo.Conductor', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Conductor (
        conductor_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Conductor PRIMARY KEY,
        nombre VARCHAR(50) NOT NULL,
        apellidos VARCHAR(50) NOT NULL,
        licencia VARCHAR(20) NOT NULL CONSTRAINT UQ_Conductor_licencia UNIQUE,
        telefono VARCHAR(20) NOT NULL
    );
END;
GO

IF OBJECT_ID(N'dbo.Vehiculo', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Vehiculo (
        vehiculo_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Vehiculo PRIMARY KEY,
        placa VARCHAR(15) NOT NULL CONSTRAINT UQ_Vehiculo_placa UNIQUE,
        capacidad_kg DECIMAL(10,2) NOT NULL CONSTRAINT CK_Vehiculo_capacidad CHECK (capacidad_kg > 0),
        estado VARCHAR(20) NOT NULL,
        empresa_id INT NOT NULL,
        CONSTRAINT FK_Vehiculo_empresa FOREIGN KEY (empresa_id) REFERENCES dbo.EmpresaLogistica(empresa_id)
    );
END;
GO

IF OBJECT_ID(N'dbo.Envio', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Envio (
        envio_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Envio PRIMARY KEY,
        codigo_rastreo VARCHAR(30) NOT NULL CONSTRAINT UQ_Envio_codigo UNIQUE,
        direccion_destino VARCHAR(200) NOT NULL,
        peso_kg DECIMAL(10,2) NOT NULL CONSTRAINT CK_Envio_peso CHECK (peso_kg > 0),
        costo DECIMAL(10,2) NOT NULL CONSTRAINT CK_Envio_costo CHECK (costo > 0),
        estado_envio VARCHAR(20) NOT NULL CONSTRAINT CK_Envio_estado CHECK (estado_envio IN ('PENDIENTE', 'EN_TRANSITO', 'ENTREGADO', 'CANCELADO')),
        vehiculo_id INT NOT NULL,
        conductor_id INT NOT NULL,
        fecha_creacion DATETIME2 NULL,
        fecha_modificacion DATETIME2 NULL,
        CONSTRAINT FK_Envio_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES dbo.Vehiculo(vehiculo_id),
        CONSTRAINT FK_Envio_conductor FOREIGN KEY (conductor_id) REFERENCES dbo.Conductor(conductor_id)
    );
END;
GO
