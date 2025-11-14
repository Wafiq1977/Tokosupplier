# TODO List for Checkout Implementation

## Current Status
- Cart page links to `/payment/dashboard` for checkout
- PaymentController handles `/payment/dashboard` and order creation
- TODO.md contains error marker for `/orders/checkout`

## Tasks
- [x] Create `/orders/checkout` endpoint in OrderController
- [x] Update cart.html to link to `/orders/checkout` instead of `/payment/dashboard`
- [ ] Move checkout logic from PaymentController to OrderController if needed
- [ ] Test the checkout flow
- [ ] Remove error marker from TODO.md

## Notes
- The `/orders/checkout` URL was marked as error in TODO.md
- Current checkout flow: Cart -> Payment Dashboard -> Create Order
- Proposed flow: Cart -> Orders Checkout -> Create Order
