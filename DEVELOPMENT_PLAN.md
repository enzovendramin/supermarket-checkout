# Supermarket Checkout System — Plano de Desenvolvimento

**Projeto:** CentraleSupélec Software Engineering 2026  
**Prazo:** 04/06/2026  
**Linguagem:** Java (Maven + JUnit 5)

---

## Estrutura do Projeto

```
SupermarketCheckout/
├── pom.xml
├── my_supermarket.ini
├── testScenario1.txt
└── src/
    ├── main/java/supermarket/
    │   ├── model/          Customer, Item, Category, Cart, CartEntry, Receipt, BankCard
    │   ├── users/          User (abstract), Manager, Cashier
    │   ├── discount/       DiscountPlan (interface), NormalPlan, PrimePlan, PlatinumPlan, DiscountPlanFactory
    │   ├── pricing/        PricingPolicy (interface), CategoryDiscount, NoPricingPolicy
    │   ├── inventory/      Inventory, StockObserver (interface), ManagerNotifier, SupplierNotifier
    │   ├── payment/        POSDevice, TransactionAuthorisationSystem, PaymentResult, PaymentSimulator
    │   ├── delivery/       DeliveryRequest, DeliveryCalculator (interface), StandardDeliveryCalculator, TimeSlot, DeliveryManager
    │   ├── register/       CashRegister, Supermarket
    │   └── cli/            CLI, CommandDispatcher, CommandContext
    └── test/java/supermarket/   (espelha src/main — um *Test.java por classe)
```

---

## Padrões de Design

| Padrão | Onde | Requisito |
|--------|------|-----------|
| **Strategy** | `DiscountPlan` → Normal/Prime/Platinum | R5b: novos planos sem modificar código existente |
| **Strategy** | `PricingPolicy` por `Category` | R6b: novas políticas de preço por categoria |
| **Observer** | `Inventory` notifica `StockObserver`s | R9: alerta de estoque baixo (obrigatório) |
| **Strategy** | `DeliveryCalculator` | R8: lógica de frete substituível |
| **Facade** | `TransactionAuthorisationSystem` | Esconde detalhes do protocolo bancário |

---

## Fases de Implementação

- [x] **Fase 1** — Estrutura Maven + Modelo de Domínio (`Item`, `Category`, `Customer`, `BankCard`, `Cart`, `CartEntry`)
- [x] **Fase 2** — Planos de Desconto: `DiscountPlan` interface + Normal/Prime/Platinum + `DiscountPlanFactory`
- [x] **Fase 3** — Políticas de Preço: `PricingPolicy` interface + `CategoryDiscount` + `NoPricingPolicy`
- [x] **Fase 4** — `CashRegister` + cálculo do bill (plano + política de categoria)
- [x] **Fase 5** — Sistema de Pagamento: `POSDevice` + `TransactionAuthorisationSystem` (TAS) + `PaymentSimulator`
- [x] **Fase 6** — Inventário + Observer: `Inventory` notifica `ManagerNotifier`/`SupplierNotifier` ao atingir threshold (R9)
- [x] **Fase 7** — Entrega Domiciliar: `StandardDeliveryCalculator` (peso+distância), descontos por plano (R8b), `TimeSlot`/`DeliveryManager` para janelas de 2h e preço dinâmico (R10)
- [x] **Fase 8** — CLUI completa: 20 comandos, tratamento de erros, `runTest`, `setup`, `simulatePayment`
- [x] **Fase 9** — `my_supermarket.ini` + cenários de teste adicionais
- [x] **Fase 10** — JUnit Tests (um `*Test.java` por classe, sem getters/setters)
- [x] **Fase 11** — UML (class diagram obrigatório + use case + sequence) + Relatório (conteúdo em `docs/`; falta só exportar o PDF)

---

## Comandos CLUI (20 no total)

| Comando | Papel | Requisito |
|---------|-------|-----------|
| `login <u> <p>` | qualquer | — |
| `logout` | qualquer | — |
| `setup` | manager | bootstrap padrão |
| `registerCashier <fn> <ln> <u> <pwd>` | manager | — |
| `registerCustomer <fn> <ln> <u> <addr> <pwd>` | manager | — |
| `addItem <name> <cat> <price> <weight> <stock>` | manager | R2b, R6b |
| `restock <name> <qty>` | manager | R9 |
| `setCategoryDiscount <cat> <pct>` | manager | R6 |
| `subscribeToPlan <plan>` | customer | R5 |
| `startCheckout <customerUser>` | cashier | R1 |
| `scanItem <name> <qty>` | cashier | R2, R3 |
| `computeBill` | cashier | R5, R6, R8 |
| `requestDelivery <address>` | customer | R7 |
| `pay <card> <pin>` | cashier | R4, R9 |
| `simulatePayment <outcome>` | cashier | teste |
| `showInventory` | manager | R9 |
| `showRevenue` | manager | — |
| `runTest <file>` | qualquer | — |
| `help` | qualquer | — |

---

## Como Verificar / Testar

```bash
mvn compile                                         # compilar
mvn test                                            # rodar JUnit
mvn exec:java -Dexec.mainClass="supermarket.cli.CLI"  # iniciar CLUI
# dentro da CLUI:
runTest testScenario1.txt
```

---

## Notas de Requisitos

- **R8 (entrega):** ≤10kg e ≤30km → €15 fixo; 10–50kg → fixo + % do total; >50kg → recusar
- **R8b:** Prime paga 50% do frete; Platinum tem frete grátis
- **R9 (Observer):** threshold configurável por item perecível (carne, laticínios)
- **R10:** janelas de 2h, anti-overbooking por capacidade de veículo, tarifa maior em horário de pico, desconto "eco" se caminhão já está na região
- **TAS:** simula banco (conexão segura, verificar PIN, verificar saldo, autorizar/recusar)
- **Usuário padrão de manager:** `ceo` / `123456789`
