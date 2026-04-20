# Coverage Report

## Загальне покриття Jest
- Statements/Instructions: 30.16%
- Branches: 9.26%
- Functions/Methods: 8.75%
- Lines: 32.07%

## Mutation Testing
- Mutation Score of total: 100%
- Mutation Score of covered: 100%
- Killed: 17
- Survived: 0
- Timeout: 0
- No coverage: 0

## Аналіз
У цій лабораторній роботі було розширено unit-тести для frontend helper/utils логіки.  
Основний фокус зроблено на файлах `getHref.js` та `sheduleUtils.js`, для яких додано нові тести на позитивні, негативні та edge-case сценарії.  
Було перевірено обробку порожніх значень, `null`, граничних випадків, а також коректність атрибутів і фільтрації даних.  
Тести написано зі зрозумілим групуванням та за AAA-патерном.  

За результатами mutation testing (Stryker) для цільових файлів отримано 100% mutation score: усі мутанти були виявлені та “вбиті”, survived mutants відсутні.  
Це підтверджує, що тести перевіряють не лише виконання рядків коду, а й правильність логіки функцій.  

Водночас загальний coverage усього frontend-проєкту все ще нижчий за глобальні пороги Jest, оскільки поточна робота охоплювала лише частину модулів.  
Для підвищення загального покриття надалі потрібно розширити тестування великих компонентів і сторінок застосунку.

## Скріншот
<img width="1280" height="402" alt="image" src="https://github.com/user-attachments/assets/64bce868-b9cf-4483-ad51-bbbaf0414fbc" />
<img width="1557" height="497" alt="image" src="https://github.com/user-attachments/assets/446e4619-1dc4-4a54-95d7-7313358275fb" />
<img width="1581" height="503" alt="image" src="https://github.com/user-attachments/assets/eced1db0-ecfa-4dd0-ab6d-b186e9d2b243" />
<img width="1587" height="506" alt="image" src="https://github.com/user-attachments/assets/d275f5d8-3b94-4b82-aa65-0b620260052d" />
