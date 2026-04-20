import { render, screen } from '@testing-library/react';
import { getHref } from './getHref';

jest.mock('../i18n', () => ({
    __esModule: true,
    default: {
        t: (key) => key,
    },
}));

describe('getHref', () => {
    describe('positive scenarios', () => {
        it('should render anchor with href and title matching the link', () => {
            // Arrange
            const link = 'https://www.youtube.com/';
            // Act
            render(getHref(link));
            const anchor = screen.getByRole('link');
            // Assert
            expect(anchor).toHaveAttribute('href', link);
            expect(anchor).toHaveAttribute('title', link);
        });

        it('should preserve link without protocol as href (relative / scheme-less)', () => {
            // Arrange
            const link = 'meet.google.com/abc-defg-hij';
            // Act
            render(getHref(link));
            const anchor = screen.getByRole('link');
            // Assert
            expect(anchor).toHaveAttribute('href', link);
            expect(anchor).toHaveAttribute('title', link);
        });

        it('should handle very long link values', () => {
            // Arrange
            const link = `https://example.com/${'x'.repeat(5000)}`;
            // Act
            render(getHref(link));
            const anchor = screen.getByRole('link');
            // Assert
            expect(anchor).toHaveAttribute('href', link);
            expect(anchor).toHaveAttribute('title', link);
        });
    });

    describe('edge cases', () => {
        it('should render empty string as href and title', () => {
            // Arrange
            const link = '';
            // Act
            render(getHref(link));
            const anchor = screen.getByRole('link');
            // Assert
            expect(anchor).toHaveAttribute('href', '');
            expect(anchor).toHaveAttribute('title', '');
        });

        it('should render when link is null without href/title attributes in the DOM', () => {
            // Arrange
            const link = null;
            // Act 
            const { container } = render(getHref(link));
            const anchor = container.querySelector('a.link-to-meeting');
            // Assert
            expect(anchor).not.toBeNull();
            expect(anchor.hasAttribute('href')).toBe(false);
            expect(anchor.hasAttribute('title')).toBe(false);
        });
    });

    describe('element attributes', () => {
        it('should set target, rel, and className for meeting link', () => {
            // Arrange
            const link = 'https://example.com/room';
            // Act
            render(getHref(link));
            const anchor = screen.getByRole('link');
            // Assert
            expect(anchor).toHaveClass('link-to-meeting');
            expect(anchor).toHaveAttribute('target', '_blank');
            expect(anchor).toHaveAttribute('rel', 'noreferrer');
        });
    });
});
