# Datos de prueba: 95 recetas para probar la paginación

Script para cargar 95 recetas ficticias en la base `recetario` (10 páginas de 10, la última con 5 filas). Está pensado para correr directo en **MongoDB Compass**.

## Instrucciones

1. Conectate a tu servidor en Compass (`mongodb://localhost:27017`).
2. Abrí la shell integrada: botón `>_ Open MongoDB shell` (abajo a la izquierda).
3. Copiá el script de abajo **completo**, pegalo en la shell y apretá Enter.
4. Refrescá la base `recetario` en el panel izquierdo: la colección `recetas` tiene las 95 recetas (más las que ya hubieras creado).

## Cosas a saber

- **Es re-ejecutable.** Antes de insertar, borra las recetas de una corrida anterior del script (las marca con `usuario_creador_id: "seed"`), así que nunca se duplican. Las recetas que creaste a mano desde la app no se tocan.
- Todo va dentro de una función que se ejecuta al momento, para poder pegarlo varias veces en la misma shell sin el error `Identifier has already been declared`.
- Si Mongo corre dentro de Docker (`docker compose up`), Compass se conecta igual a `localhost:27017`, porque el compose publica ese puerto.
- Ver [PAGINACION.md](PAGINACION.md) para el detalle de cómo funciona la paginación.

## Script

```js
(function () {
  const coleccion = db.getSiblingDB("recetario").recetas;

  const SEED_ID = "seed";
  const CANTIDAD = 95;

  // [nombre, categoría, minutos base, porciones base, vegetariana]
  const platos = [
    ["Milanesa de pollo", "Carnes", 35, 4, false],
    ["Ensalada caprese", "Ensaladas", 10, 2, true],
    ["Ravioles de ricota", "Pastas", 40, 4, true],
    ["Tarta de espinaca", "Tartas", 50, 6, true],
    ["Empanadas de carne", "Entradas", 60, 12, false],
    ["Sopa de calabaza", "Sopas", 45, 4, true],
    ["Risotto de hongos", "Arroces", 40, 3, true],
    ["Pastel de papa", "Carnes", 70, 6, false],
    ["Guiso de lentejas", "Guisos", 80, 6, false],
    ["Tortilla de papas", "Entradas", 30, 4, true],
    ["Flan casero", "Postres", 60, 8, true],
    ["Brownie de chocolate", "Postres", 45, 9, true],
    ["Salmón al horno", "Pescados", 30, 2, false],
    ["Wok de vegetales", "Salteados", 20, 2, true],
    ["Ñoquis de papa", "Pastas", 55, 4, true],
    ["Pollo al limón", "Carnes", 50, 4, false],
    ["Ensalada César", "Ensaladas", 20, 2, false],
    ["Hamburguesa casera", "Carnes", 25, 2, false],
    ["Pizza margarita", "Pizzas", 90, 4, true],
    ["Panqueques con dulce de leche", "Postres", 30, 6, true],
  ];
  const variantes = ["", "a la crema", "express", "light", "de la abuela"];

  const borradas = coleccion.deleteMany({ usuario_creador_id: SEED_ID }).deletedCount;

  const ahora = new Date();
  const docs = [];
  for (let i = 0; i < CANTIDAD; i++) {
    const [nombre, categoria, minutos, porciones, vegetariana] = platos[i % platos.length];
    const variante = variantes[Math.floor(i / platos.length)];
    docs.push({
      nombre: `${nombre} ${variante}`.trim(),
      categoria,
      tiempo_preparacion_minutos: minutos + (i % 7) * 5,
      porciones: Math.max(1, porciones + (i % 3) - 1),
      vegetariana,
      usuario_creador_id: SEED_ID,
      usuario_actualizador_id: SEED_ID,
      fecha_creacion: ahora,
      fecha_actualizacion: ahora,
    });
  }

  coleccion.insertMany(docs);
  print(`Borradas de una corrida anterior: ${borradas}`);
  print(`Insertadas: ${docs.length}`);
  print(`Total en "recetario.recetas": ${coleccion.countDocuments({})}`);
})();
```
