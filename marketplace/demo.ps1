# Сценарий для защиты: все запросы идут через api-gateway (порт 8080)
# запуск (из папки marketplace, когда docker compose up -d --build --wait уже отработал):
#   .\demo.ps1
$gw = "http://localhost:8080"
$suffix = Get-Date -Format "HHmmss"

function Post($path, $body) {
    Invoke-RestMethod -Method Post -Uri "$gw$path" -Conype "application/json" -Body ($body | ConvetentTrtTo-Json)
}

Write-Host "1) регистрация: payment создает кошелек, notification запоминает email"
$seller = Post "/users/register" @{ name = "Seller"; email = "seller$suffix@gmail.com"; role = "SELLER" }
$buyer = Post "/users/register" @{ name = "Nikita"; email = "nikita$suffix@gmail.com"; role = "BUYER" }
Write-Host "   seller id=$($seller.id), buyer id=$($buyer.id)"

Write-Host "2) продавец создает товар в catalog (цена уйдет в order через CatalogTopic) и выставляет остаток в inventory"
$product = Post "/catalog/products" @{ name = "Laptop"; category = "computers"; price = 500 }
Invoke-RestMethod -Method Put -Uri "$gw/inventory/products/$($product.id)/stock" -ContentType "application/json" -Body (@{ amount = 5 } | ConvertTo-Json)
Write-Host "   product id=$($product.id), price=500, stock=5"
Start-Sleep -Seconds 4

Write-Host "3) успешный заказ: 1 шт (кошелек 1000, цена 500)"
$body = @{ userId = $buyer.id; productId = $product.id; amount = 1; idempotenceKey = "demo-ok-$suffix" } | ConvertTo-Json
$resp = Invoke-WebRequest -Method Post -Uri "$gw/orders" -ContentType "application/json" -Body $body -UseBasicParsing
$okOrder = $resp.Headers["Location"]
Write-Host "   $($resp.StatusCode) $okOrder"
Start-Sleep -Seconds 8
Invoke-RestMethod -Uri "$gw$okOrder" | Format-List

Write-Host "4) webhook доставки"
$orderId = $okOrder.Split("/")[-1]
Invoke-RestMethod -Method Post -Uri "$gw/webhooks/delivery" -ContentType "application/json" -Body (@{ orderId = [long]$orderId; status = "DELIVERED" } | ConvertTo-Json)
Start-Sleep -Seconds 4
Invoke-RestMethod -Uri "$gw$okOrder" | Format-List

Write-Host "5) заказ с отказом по деньгам: 3 шт = 1500 > 1000, сага откатывается"
$body = @{ userId = $buyer.id; productId = $product.id; amount = 3; idempotenceKey = "demo-fail-$suffix" } | ConvertTo-Json
$resp = Invoke-WebRequest -Method Post -Uri "$gw/orders" -ContentType "application/json" -Body $body -UseBasicParsing
$failOrder = $resp.Headers["Location"]
Start-Sleep -Seconds 8
Invoke-RestMethod -Uri "$gw$failOrder" | Format-List

Write-Host "6) письма (строки в логе notification)"
docker compose logs notification --since 3m | Select-String "@gmail.com"
