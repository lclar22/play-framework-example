
$ ->
  $.get "/requestRowProductors", (rows) ->
    $.each rows, (index, row) ->
      requestRowId = $("<td>").text row.requestRowId
      productId = $("<td>").text row.productId
      productorId = $("<td>").text row.productorId
      quantity = $("<td>").text row.quantity
      precio = $("<td>").text row.precio
      paid = $("<td>").text row.paid
      status = $("<td>").text row.status
      links = $("<td>").html '<a href="/requestRowProductor_cancel/' + row.id + '" class="btn btn-danger">Cancelar</a>' + '<a href="/requestRowProductor_fill/' + row.id + '" class="btn btn-primary">Entregar</a>' + '<a href="/requestRowProductor_update/' + row.id + '" class="btn btn-primary">Editar</a>' + '<a href="/requestRowProductor_remove/' + row.id + '" class="btn btn-danger">Eliminar</a>' + '<a href="/requestRowProductor_show/' + row.id + '" class="btn btn-info">Mostrar</a>'
      $("#rows").append $("<tr>").append(requestRowId).append(productId).append(productorId).append(quantity).append(precio).append(paid).append(status).append(links)
