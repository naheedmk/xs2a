# Release notes v.2.6

## Bugfix: Payment initiation request fails for non-json payment products
Executing valid initiate payment request (`POST /v1/{payment-service}/{payment-product}`) with non-json body will no 
longer result in `400 FORMAT_ERROR` being returned in the response. 
