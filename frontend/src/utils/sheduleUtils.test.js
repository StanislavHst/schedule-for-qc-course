import { getScheduleByType, isNotReadySchedule, filterClassesArray } from './sheduleUtils';

describe('sheduleUtils', () => {
    describe('getScheduleByType', () => {
        it('should expose two parameters (entityId, semesterId)', () => {
            // Arrange & Act 
            // Assert
            expect(getScheduleByType.length).toBe(2);
        });

        it('should return a plain empty object for given ids', () => {
            // Arrange
            const entityId = 42;
            const semesterId = 7;
            // Act
            const result = getScheduleByType(entityId, semesterId);
            // Assert
            expect(result).toEqual({});
            expect(result).not.toBeUndefined();
            expect(typeof result).toBe('object');
            expect(Object.keys(result)).toHaveLength(0);
        });
    });

    describe('isNotReadySchedule', () => {
        describe('empty schedule', () => {
            it('should be true when schedule is empty object and not loading', () => {
                // Arrange
                const schedule = {};
                const loading = false;
                // Act
                const result = isNotReadySchedule(schedule, loading);
                // Assert
                expect(result).toBe(true);
            });

            it('should be false when schedule is empty object but loading', () => {
                // Arrange
                const schedule = {};
                const loading = true;
                // Act
                const result = isNotReadySchedule(schedule, loading);
                // Assert
                expect(result).toBe(false);
            });

            it('should be true when schedule is null and not loading', () => {
                // Arrange
                const schedule = null;
                const loading = false;
                // Act
                const result = isNotReadySchedule(schedule, loading);
                // Assert
                expect(result).toBe(true);
            });

            it('should be false when schedule is null but loading', () => {
                // Arrange
                const schedule = null;
                const loading = true;
                // Act
                const result = isNotReadySchedule(schedule, loading);
                // Assert
                expect(result).toBe(false);
            });

            it('should be true when schedule is empty array and not loading', () => {
                // Arrange
                const schedule = [];
                const loading = false;
                // Act
                const result = isNotReadySchedule(schedule, loading);
                // Assert
                expect(result).toBe(true);
            });

            it('should be false when schedule is empty array but loading', () => {
                // Arrange
                const schedule = [];
                const loading = true;
                // Act
                const result = isNotReadySchedule(schedule, loading);
                // Assert
                expect(result).toBe(false);
            });
        });

        describe('non-empty schedule', () => {
            it('should be false when schedule has keys and not loading', () => {
                // Arrange
                const schedule = { monday: [] };
                const loading = false;
                // Act
                const result = isNotReadySchedule(schedule, loading);
                // Assert
                expect(result).toBe(false);
            });

            it('should be false when schedule has keys even if loading', () => {
                // Arrange
                const schedule = { monday: [] };
                const loading = true;
                // Act
                const result = isNotReadySchedule(schedule, loading);
                // Assert
                expect(result).toBe(false);
            });
        });
    });

    describe('filterClassesArray', () => {
        it('should return empty array for empty input', () => {
            // Arrange
            const input = [];
            // Act
            const result = filterClassesArray(input);
            // Assert
            expect(result).toEqual([]);
        });

        it('should return same single-element array', () => {
            // Arrange
            const input = [{ id: 1, name: 'A' }];
            // Act
            const result = filterClassesArray(input);
            // Assert
            expect(result).toEqual([{ id: 1, name: 'A' }]);
        });

        it('should remove duplicate ids keeping first occurrence', () => {
            // Arrange
            const first = { id: 10, label: 'first' };
            const dup = { id: 10, label: 'duplicate' };
            const other = { id: 20, label: 'other' };
            const input = [first, dup, other];
            // Act
            const result = filterClassesArray(input);
            // Assert
            expect(result).toEqual([first, other]);
        });

        it('should collapse several consecutive duplicates to a single item', () => {
            // Arrange
            const a = { id: 1, n: 'a' };
            const input = [a, { id: 1, n: 'b' }, { id: 1, n: 'c' }];
            // Act
            const result = filterClassesArray(input);
            // Assert
            expect(result).toEqual([a]);
        });

        it('should keep all items when ids are unique', () => {
            // Arrange
            const input = [
                { id: 1, v: 'a' },
                { id: 2, v: 'b' },
                { id: 3, v: 'c' },
            ];
            // Act
            const result = filterClassesArray(input);
            // Assert
            expect(result).toHaveLength(3);
            expect(result).toEqual(input);
        });

        it('should throw when input is null', () => {
            // Arrange
            const input = null;
            // Act & Assert
            expect(() => filterClassesArray(input)).toThrow(TypeError);
        });
    });
});
