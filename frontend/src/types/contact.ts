export interface Contact {
  contactId: string;
  name: string;
  email: string;
  phoneNumber: string;
}

export type ContactFormValues = Omit<Contact, "contactId">;