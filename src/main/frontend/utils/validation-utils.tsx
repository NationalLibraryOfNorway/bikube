export function validateBetweenZeroAndFive(value: number) {
    let error;
    if (value < 0) {
        error = 'Tallet kan ikke være negativt';
    } else if (value > 5) {
        error = 'Tallet kan ikke være større enn 5';
    }
    return error;
}

export function checkDuplicateEditions(editions: string[]) {
    let duplicateFound = false;
    for (const ed of editions) {
        if (editions.filter(v => !!v && v === ed).length > 1) {
            duplicateFound = true;
            break;
        }
    }
    return duplicateFound ? 'Det fins duplikate utgavenummer' : '';
}
